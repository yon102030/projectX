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

public class DatabaseService {

	private static final String TAG = "DatabaseService";

	private static final String USERS_PATH = "users";
	private static final String CLOTHES_PATH = "clothe";
	private static final String OUTFITS_PATH = "outfit";

	public interface DatabaseCallback<T> {
		void onCompleted(T object);
		void onFailed(Exception e);
	}

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
	// בסיס
	// =============================

	private DatabaseReference readData(String path) {
		return databaseReference.child(path);
	}

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

	private <T> void getData(String path, Class<T> clazz, DatabaseCallback<T> callback) {
		readData(path).get().addOnCompleteListener(task -> {

			if (!task.isSuccessful()) {
				callback.onFailed(task.getException());
				return;
			}

			try {
				callback.onCompleted(task.getResult().getValue(clazz));
			} catch (Exception e) {
				callback.onFailed(e);
			}
		});
	}

	// 🔥 תיקון חשוב פה (מונע קריסה מנתונים ישנים)
	private <T> void getDataList(String path, Class<T> clazz, DatabaseCallback<List<T>> callback) {

		readData(path).get().addOnCompleteListener(task -> {

			if (!task.isSuccessful()) {
				callback.onFailed(task.getException());
				return;
			}

			List<T> list = new ArrayList<>();

			for (DataSnapshot snap : task.getResult().getChildren()) {

				try {

					// מדלג על נתונים שבורים (String וכו')
					if (!snap.hasChildren()) continue;

					T item = snap.getValue(clazz);

					if (item != null) {
						list.add(item);
					}

				} catch (Exception e) {
					Log.e(TAG, "Bad data skipped: " + snap.getKey(), e);
				}
			}

			callback.onCompleted(list);
		});
	}

	private String generateNewId(String path) {
		return databaseReference.child(path).push().getKey();
	}

	// =============================
	// USERS
	// =============================

	public void createNewUser(User user, DatabaseCallback<String> callback) {

		FirebaseAuth.getInstance()
				.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
				.addOnCompleteListener(task -> {

					if (!task.isSuccessful()) {
						callback.onFailed(task.getException());
						return;
					}

					String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
					user.setUserId(uid);

					writeData(USERS_PATH + "/" + uid, user, new DatabaseCallback<Void>() {
						@Override
						public void onCompleted(Void object) {
							callback.onCompleted(uid);
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
	// CLOTHES
	// =============================

	public void createNewClothe(Clothe clothe, DatabaseCallback<Void> callback) {
		writeData(CLOTHES_PATH + "/" + clothe.getItemId(), clothe, callback);
	}

	public void getClothe(String id, DatabaseCallback<Clothe> callback) {
		getData(CLOTHES_PATH + "/" + id, Clothe.class, callback);
	}

	public void getClotheList(DatabaseCallback<List<Clothe>> callback) {
		getDataList(CLOTHES_PATH, Clothe.class, callback);
	}

	public String generateClotheId() {
		return generateNewId(CLOTHES_PATH);
	}

	public void deleteClothe(String id, DatabaseCallback<Void> callback) {
		deleteData(CLOTHES_PATH + "/" + id, callback);
	}

	public void getUserClothes(String userId, DatabaseCallback<List<Clothe>> callback) {

		getClotheList(new DatabaseCallback<List<Clothe>>() {
			@Override
			public void onCompleted(List<Clothe> clothes) {

				List<Clothe> userClothes = new ArrayList<>();

				for (Clothe c : clothes) {
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

	// =============================
	// OUTFITS
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