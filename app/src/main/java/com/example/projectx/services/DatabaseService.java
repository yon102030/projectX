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

// מחלקה זו משמשת כ"מנהל התקשורת" מול מסד הנתונים (Firebase Realtime Database).
// היא מרכזת את כל פעולות הקריאה, הכתיבה והמחיקה למקום אחד מסודר.
public class DatabaseService {

	private static final String TAG = "DatabaseService";

	// הגדרת "נתיבים" (תיקיות) בתוך מסד הנתונים בפיירבייס
	private static final String USERS_PATH = "users";
	private static final String CLOTHES_PATH = "clothe";
	private static final String OUTFITS_PATH = "outfit";

	// ממשק (Callback) מותאם אישית:
	// מכיוון שפנייה לאינטרנט לוקחת זמן, הפונקציות לא מחזירות מיד תשובה.
	// במקום זאת, אנחנו מעבירים להן את ה-Callback הזה, וכשהתשובה מגיעה מהשרת - הוא יפעיל את onCompleted (בהצלחה) או onFailed (בכישלון).
	public interface DatabaseCallback<T> {
		void onCompleted(T object);
		void onFailed(Exception e);
	}

	// יישום תבנית Singleton: מוודא שתמיד נעבוד עם אותו עותק (instance) של המחלקה בכל האפליקציה
	private static DatabaseService instance;
	private final DatabaseReference databaseReference;

	public DatabaseService() {
		databaseReference = FirebaseDatabase.getInstance().getReference();
	}

	public static DatabaseService getInstance() {
		if (instance == null) {
			instance = new DatabaseService();
		}
		return instance;
	}

	// =============================
	// בסיס (פעולות תשתית גנריות שמשמשות את שאר הפונקציות)
	// =============================

	// פונקציית עזר להגעה לנתיב ספציפי במסד הנתונים
	private DatabaseReference readData(String path) {
		return databaseReference.child(path);
	}

	// פונקציה כללית לכתיבת נתונים מכל סוג (Object) לתוך נתיב מסוים
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

	// פונקציה כללית למחיקת נתונים
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

	// פונקציה גנרית (מקבלת סוג <T> כמו User.class או Clothe.class) לשליפת פריט בודד
	private <T> void getData(String path, Class<T> clazz, DatabaseCallback<T> callback) {
		readData(path).get().addOnCompleteListener(task -> {

			if (!task.isSuccessful()) {
				callback.onFailed(task.getException());
				return;
			}

			try {
				// ממיר את המידע הגולמי שחזר מפיירבייס לאובייקט מהסוג שביקשנו (למשל, אובייקט מסוג User)
				callback.onCompleted(task.getResult().getValue(clazz));
			} catch (Exception e) {
				callback.onFailed(e);
			}
		});
	}

	// פונקציה חכמה למשיכת רשימה של פריטים (למשל, כל הבגדים או כל המשתמשים)
	// 🔥 תיקון חשוב פה (מונע קריסה מנתונים ישנים)
	private <T> void getDataList(String path, Class<T> clazz, DatabaseCallback<List<T>> callback) {

		readData(path).get().addOnCompleteListener(task -> {

			if (!task.isSuccessful()) {
				callback.onFailed(task.getException());
				return;
			}

			List<T> list = new ArrayList<>();

			// עובר על כל הפריטים (ה"ילדים") שנמצאים תחת הנתיב שביקשנו
			for (DataSnapshot snap : task.getResult().getChildren()) {

				try {

					// הגנה קריטית: מדלג על נתונים שבורים או מידע ישן (כמו טקסט סתמי) שלא תואם למבנה שלנו
					// כדי למנוע קריסה של כל האפליקציה בגלל רשומה אחת פגומה במסד הנתונים.
					if (!snap.hasChildren()) continue;

					T item = snap.getValue(clazz);

					if (item != null) {
						list.add(item);
					}

				} catch (Exception e) {
					// מדפיס את השגיאה ליומן (Log) אבל ממשיך לטעון את שאר הפריטים התקינים!
					Log.e(TAG, "Bad data skipped: " + snap.getKey(), e);
				}
			}

			callback.onCompleted(list); // מחזיר את הרשימה המוכנה למסך שביקש אותה
		});
	}

	// מייצר מזהה (ID) אקראי וייחודי לגמרי דרך פיירבייס (למשל כשיוצרים בגד חדש)
	private String generateNewId(String path) {
		return databaseReference.child(path).push().getKey();
	}

	// =============================
	// USERS (פעולות הקשורות למשתמשים)
	// =============================

	// פונקציה מיוחדת שיוצרת משתמש חדש: היא משלבת עבודה מול 2 מערכות שונות של פיירבייס
	public void createNewUser(User user, DatabaseCallback<String> callback) {

		// 1. קודם כל, יוצרים למשתמש חשבון התחברות (במערכת ה-Authentication של פיירבייס)
		FirebaseAuth.getInstance()
				.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
				.addOnCompleteListener(task -> {

					if (!task.isSuccessful()) {
						callback.onFailed(task.getException());
						return;
					}

					// אם היצירה הצליחה, שולפים את ה-ID האמיתי שפיירבייס יצר לו עכשיו
					String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
					user.setUserId(uid);

					// 2. כעת שומרים את כל פרטי המשתמש (שם, טלפון וכו') בתוך מסד הנתונים (Realtime Database)
					writeData(USERS_PATH + "/" + uid, user, new DatabaseCallback<Void>() {
						@Override
						public void onCompleted(Void object) {
							callback.onCompleted(uid); // מחזירים למסך ההרשמה שהכל עבד, יחד עם ה-ID
						}

						@Override
						public void onFailed(Exception e) {
							callback.onFailed(e);
						}
					});
				});
	}

	public void getUser(String uid, DatabaseCallback<User> callback) {
		getData(USERS_PATH + "/" + uid, User.class, callback);
	}

	public void getUserList(DatabaseCallback<List<User>> callback) {
		getDataList(USERS_PATH, User.class, callback);
	}

	public void deleteUser(String userId, DatabaseCallback<Void> callback) {
		deleteData(USERS_PATH + "/" + userId, callback);
	}

	// =============================
	// CLOTHES (פעולות הקשורות לבגדים)
	// =============================

	public void createNewClothe(Clothe clothe, DatabaseCallback<Void> callback) {
		writeData(CLOTHES_PATH + "/" + clothe.getItemId(), clothe, callback);
	}

	public void getClothe(String id, DatabaseCallback<Clothe> callback) {
		getData(CLOTHES_PATH + "/" + id, Clothe.class, callback);
	}

	// מושכת את כלללל הבגדים שבמערכת (שימושי למסך ניהול הפריטים של ה-Admin)
	public void getClotheList(DatabaseCallback<List<Clothe>> callback) {
		getDataList(CLOTHES_PATH, Clothe.class, callback);
	}

	public String generateClotheId() {
		return generateNewId(CLOTHES_PATH);
	}

	public void deleteClothe(String id, DatabaseCallback<Void> callback) {
		deleteData(CLOTHES_PATH + "/" + id, callback);
	}

	// פונקציה חכמה שמושכת את כל הבגדים של המערכת, אבל מסננת ומחזירה רק את הבגדים
	// של משתמש ספציפי (לפי ה-ID שלו). זה מה שמפעיל את מחולל הלוקים.
	public void getUserClothes(String userId, DatabaseCallback<List<Clothe>> callback) {

		getClotheList(new DatabaseCallback<List<Clothe>>() {
			@Override
			public void onCompleted(List<Clothe> clothes) {

				List<Clothe> userClothes = new ArrayList<>();

				// עוברים על כל רשימת הבגדים ומחפשים למי שייך כל בגד
				for (Clothe c : clothes) {
					if (c != null && Objects.equals(c.getUserId(), userId)) {
						userClothes.add(c);
					}
				}

				callback.onCompleted(userClothes); // מחזירים רק את הבגדים של המשתמש הזה
			}

			@Override
			public void onFailed(Exception e) {
				callback.onFailed(e);
			}
		});
	}

	// =============================
	// OUTFITS (פעולות הקשורות ללוקים שלמים)
	// =============================

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

	public void deleteOutfit(String id, DatabaseCallback<Void> callback) {
		deleteData(OUTFITS_PATH + "/" + id, callback);
	}

	// עובדת באותו היגיון כמו getUserClothes - שולפת הכל ואז מסננת רק את האאוטפיטים
	// השייכים למשתמש הספציפי שביקש לראות את "הלוקים השמורים" שלו.
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