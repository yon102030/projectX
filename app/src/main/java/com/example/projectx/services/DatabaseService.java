package com.example.projectx.services;

import android.util.Log;

import com.example.projectx.model.Clothe;
import com.example.projectx.model.Outfit;
import com.example.projectx.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/*
 * מחלקת DatabaseService:
 * זוהי ה"שכבה" שמנהלת את כל התקשורת מול מסד הנתונים Firebase Realtime Database.
 * במקום שכל מסך יפנה לפיירבייס בעצמו, כולם פונים למחלקה הזו.
 * זה עושה את הקוד נקי, מרוכז וקל לתחזוקה.
 */
public class DatabaseService {

	private static final String TAG = "DatabaseService";

	// נתיבים (Path) ב-Firebase: אלו ה"תיקיות" שבהן נשמר המידע במסד הנתונים.
	private static final String USERS_PATH = "users";
	private static final String CLOTHES_PATH = "clothe";
	private static final String OUTFITS_PATH = "outfit";

	/*
	 * ממשק DatabaseCallback:
	 * פנייה לאינטרנט פועלת ברקע (Asynchronous) ולוקחת זמן. לכן פונקציות פה לא מחזירות ערך עם 'return'.
	 * במקום זה, המסך (למשל UserPage) מעביר את ה-Callback הזה, וכשהתשובה מפיירבייס מגיעה,
	 * המערכת מפעילה את onCompleted (בהצלחה) או onFailed (במקרה של שגיאה/חוסר אינטרנט).
	 */
	public interface DatabaseCallback<T> {
		void onCompleted(T object);
		void onFailed(Exception e);
	}

	// תבנית עיצוב מסוג Singleton:
	// מבטיחה שבכל רגע נתון יהיה רק עותק אחד (Instance) של מחלקת החיבור לפיירבייס, כדי לחסוך בזיכרון ולמנוע כפילויות.
	private static DatabaseService instance;
	private final DatabaseReference databaseReference;

	// בנאי פרטי (Private): אף מחלקה אחרת לא יכולה ליצור עותק חדש בעזרת 'new DatabaseService()'
	private DatabaseService() {
		databaseReference = FirebaseDatabase.getInstance().getReference();
	}

	// זו הפונקציה היחידה דרכה שאר המסכים משיגים את מחלקת התקשורת.
	// שימוש לדוגמה: DatabaseService.getInstance().getUser(...)
	public static DatabaseService getInstance() {
		if (instance == null) {
			instance = new DatabaseService();
		}
		return instance;
	}

	// ==========================================
	// פעולות תשתית (פונקציות פנימיות ופרטיות שעושות את העבודה השחורה)
	// ==========================================

	// מנווטת לתיקייה הספציפית ב-Firebase לפי שם הנתיב (למשל "users/12345")
	private DatabaseReference readData(String path) {
		return databaseReference.child(path);
	}

	// כותבת (או מעדכנת/דורסת) נתונים בנתיב מסוים באמצעות הפקודה setValue.
	private void writeData(String path, Object data, DatabaseCallback<Void> callback) {
		readData(path).setValue(data, (error, ref) -> {
			if (callback == null) return;
			if (error != null) {
				callback.onFailed(error.toException());
			} else {
				callback.onCompleted(null);
			}
		});
	}

	// מוחקת נתונים מנתיב מסוים באמצעות הפקודה removeValue.
	private void deleteData(String path, DatabaseCallback<Void> callback) {
		readData(path).removeValue((error, ref) -> {
			if (callback == null) return;
			if (error != null) {
				callback.onFailed(error.toException());
			} else {
				callback.onCompleted(null);
			}
		});
	}

	// שולפת אובייקט בודד מהשרת.
	// Class<T> clazz: אומר לפיירבייס לאיזה מודל להמיר את הנתון שחזר (למשל להמיר ל-User.class).
	private <T> void getData(String path, Class<T> clazz, DatabaseCallback<T> callback) {
		readData(path).get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) {
				callback.onFailed(task.getException());
				return;
			}
			try {
				// ממיר את אוסף הנתונים הגולמי מפיירבייס לאובייקט Java תקין
				callback.onCompleted(task.getResult().getValue(clazz));
			} catch (Exception e) {
				callback.onFailed(e);
			}
		});
	}

	// שולפת רשימה שלמה (כמו רשימת כל המשתמשים או הבגדים).
	private <T> void getDataList(String path, Class<T> clazz, DatabaseCallback<List<T>> callback) {
		readData(path).get().addOnCompleteListener(task -> {
			if (!task.isSuccessful()) {
				callback.onFailed(task.getException());
				return;
			}

			List<T> list = new ArrayList<>();
			// לולאה שעוברת על כל הפריטים בתיקייה (למשל על כל המשתמשים בתיקיית users)
			for (DataSnapshot snap : task.getResult().getChildren()) {
				try {
					// הגנה: אם הפריט פגום, ריק או חסר לו נתונים, מדלגים עליו בעזרת continue כדי לא לקרוס
					if (!snap.hasChildren()) continue;

					T item = snap.getValue(clazz);
					if (item != null) {
						list.add(item); // מוסיף את הפריט התקין לרשימה
					}
				} catch (Exception e) {
					// אם פריט אחד נכשל, ההערה נרשמת בלוג והלולאה ממשיכה לפריט הבא בלי לקרוס!
					Log.e(TAG, "Bad data skipped: " + snap.getKey(), e);
				}
			}
			callback.onCompleted(list); // מחזיר את הרשימה המוכנה
		});
	}

	// מייצרת תעודת זהות (Key) רנדומלית וייחודית מול פיירבייס (נראה כמו "-M123abc456") כדי למנוע התנגשויות.
	private String generateNewId(String path) {
		return databaseReference.child(path).push().getKey();
	}

	// ==========================================
	// USERS (פונקציות לניהול משתמשים)
	// ==========================================

	/* * פונקציית יצירת משתמש (מופעלת במסך Register).
	 * עובדת ב-2 שלבים:
	 * 1. יוצרת חשבון אימות (Auth) של פיירבייס באמצעות אימייל וסיסמה.
	 * 2. לאחר הצלחה, לוקחת את ה-UID (תעודת הזהות שהונפקה) ושומרת את שאר פרטי המשתמש ב-Realtime Database.
	 */
	public void createNewUser(User user, DatabaseCallback<String> callback) {
		FirebaseAuth.getInstance()
				.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
				.addOnCompleteListener(task -> {
					if (!task.isSuccessful()) {
						callback.onFailed(task.getException());
						return;
					}

					String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
					user.setUserId(uid); // מצמידים למודל את ה-ID שקיבלנו מהשרת

					// שמירת המידע בנתיב users/UID
					writeData(USERS_PATH + "/" + uid, user, new DatabaseCallback<Void>() {
						@Override
						public void onCompleted(Void object) {
							callback.onCompleted(uid); // מחזיר למסך ההרשמה את ה-ID למקרה שצריך
						}
						@Override
						public void onFailed(Exception e) {
							callback.onFailed(e);
						}
					});
				});
	}

	// שולף משתמש אחד. מופעל במסך ניהול מנהל או כשצריך למשוך את הצבעים של המשתמש במסך colorpage.
	public void getUser(String uid, DatabaseCallback<User> callback) {
		getData(USERS_PATH + "/" + uid, User.class, callback);
	}

	// שולף את רשימת כלל המשתמשים. מופעל במסך AdminManageActivity (ניהול משתמשים).
	public void getUserList(DatabaseCallback<List<User>> callback) {
		getDataList(USERS_PATH, User.class, callback);
	}

	// מוחק משתמש מה-Database. מופעל על ידי המנהל.
	public void deleteUser(String userId, DatabaseCallback<Void> callback) {
		deleteData(USERS_PATH + "/" + userId, callback);
	}

	/* * מעדכן נתוני משתמש קיים (מופעל בסוף מסך colorpage לשמירת הצבעים).
	 * משתמש ב-Transaction: פקודה חכמה שמוודאת שהנתונים נכתבים בבטחה וללא התנגשות
	 * במקרה שהמשתמש מנסה לשמור נתונים מכמה מכשירים במקביל.
	 */
	public void updateUser(User user, DatabaseCallback<Void> callback) {
		readData(USERS_PATH + "/" + user.getUserId()).runTransaction(new Transaction.Handler() {
			@Override
			public Transaction.Result doTransaction(MutableData currentData) {
				currentData.setValue(user); // דורס את הנתונים הישנים עם האובייקט החדש והמעודכן
				return Transaction.success(currentData);
			}

			@Override
			public void onComplete(DatabaseError error, boolean committed, DataSnapshot currentData) {
				if (callback != null) {
					if (error != null) {
						callback.onFailed(error.toException());
					} else {
						callback.onCompleted(null);
					}
				}
			}
		});
	}

	// ==========================================
	// CLOTHES (פונקציות לניהול בגדים)
	// ==========================================

	// מופעל ממסך AddClothe לשמירת פריט לבוש חדש.
	public void createNewClothe(Clothe clothe, DatabaseCallback<Void> callback) {
		writeData(CLOTHES_PATH + "/" + clothe.getItemId(), clothe, callback);
	}

	// שולף פריט לבוש ספציפי (פחות בשימוש ישיר באפליקציה).
	public void getClothe(String id, DatabaseCallback<Clothe> callback) {
		getData(CLOTHES_PATH + "/" + id, Clothe.class, callback);
	}

	// שולף את כל הבגדים במערכת (מופעל בעיקר לצרכי סטטיסטיקה במסך AdminStatsActivity).
	public void getClotheList(DatabaseCallback<List<Clothe>> callback) {
		getDataList(CLOTHES_PATH, Clothe.class, callback);
	}

	// מייצר ID לבגד לפני שמעלים אותו לשרת.
	public String generateClotheId() {
		return generateNewId(CLOTHES_PATH);
	}

	// מוחק בגד (מופעל כשמשתמש מוחק פריט מהארון שלו).
	public void deleteClothe(String id, DatabaseCallback<Void> callback) {
		deleteData(CLOTHES_PATH + "/" + id, callback);
	}

	/* * פונקציה מסננת: שולפת את כל הבגדים בשרת, אבל מחזירה למסך (למשל user2Activity)
	 * אך ורק את הבגדים שה-userId שלהם תואם ל-ID של המשתמש המחובר כרגע.
	 * כך בונים "ארון בגדים אישי" לכל משתמש מבלי שיראה בגדים של אחרים.
	 */
	public void getUserClothes(String userId, DatabaseCallback<List<Clothe>> callback) {
		getClotheList(new DatabaseCallback<List<Clothe>>() {
			@Override
			public void onCompleted(List<Clothe> clothes) {
				List<Clothe> userClothes = new ArrayList<>();
				for (Clothe c : clothes) {
					// בדיקת שייכות: האם הבגד שייך למשתמש הנוכחי?
					if (c != null && Objects.equals(c.getUserId(), userId)) {
						userClothes.add(c);
					}
				}
				callback.onCompleted(userClothes);
			}
			@Override
			public void onFailed(Exception e) {
				callback.onFailed(e);
			}
		});
	}

	// ==========================================
	// OUTFITS (פונקציות לניהול לוקים/אאוטפיטים)
	// ==========================================

	// מופעל כשהמשתמש רואה לוק יפה ושומר אותו מתוך מחולל הלוקים.
	public void createNewOutfit(Outfit outfit, DatabaseCallback<Void> callback) {
		writeData(OUTFITS_PATH + "/" + outfit.getOutfitId(), outfit, callback);
	}

	public void getOutfit(String id, DatabaseCallback<Outfit> callback) {
		getData(OUTFITS_PATH + "/" + id, Outfit.class, callback);
	}

	public void getOutfitList(DatabaseCallback<List<Outfit>> callback) {
		getDataList(OUTFITS_PATH, Outfit.class, callback);
	}

	public String generateOutfitId() {
		return generateNewId(OUTFITS_PATH);
	}

	// מופעל כשהמשתמש מוחק לוק מתוך מסך הלוקים השמורים שלו.
	public void deleteOutfit(String id, DatabaseCallback<Void> callback) {
		deleteData(OUTFITS_PATH + "/" + id, callback);
	}

	/*
	 * פונקציה מסננת: פועלת באותו היגיון כמו getUserClothes.
	 * שולפת את כל הלוקים בענן, ומחזירה למסך savedlooks אך ורק את הלוקים
	 * שנוצרו על ידי המשתמש המחובר כרגע.
	 */
	public void getUserOutfitList(String uid, DatabaseCallback<List<Outfit>> callback) {
		getOutfitList(new DatabaseCallback<List<Outfit>>() {
			@Override
			public void onCompleted(List<Outfit> outfits) {
				List<Outfit> userOutfits = new ArrayList<>();
				for (Outfit o : outfits) {
					if (o != null && Objects.equals(o.getUserId(), uid)) {
						userOutfits.add(o);
					}
				}
				callback.onCompleted(userOutfits);
			}
			@Override
			public void onFailed(Exception e) {
				callback.onFailed(e);
			}
		});
	}
}