package com.example.projectx.services;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.projectx.model.Outfit;
import com.example.projectx.model.Clothe;
import com.example.projectx.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.MutableData;
import com.google.firebase.database.Transaction;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.UnaryOperator;

/// a service to interact with the Firebase Realtime Database.
/// this class is a singleton, use getInstance() to get an instance of this class
/// @see #getInstance()
/// @see FirebaseDatabase
public class DatabaseService {

    private static final String TAG = "DatabaseService";

    private static final String USERS_PATH = "users",
            CLOTHES_PATH = "clothe",
            OUTFITS_PATH = "outfit";

    public interface DatabaseCallback<T> {
        void onCompleted(T object);
        void onFailed(Exception e);
    }

    private static DatabaseService instance;

    private final DatabaseReference databaseReference;

    public DatabaseService() {
        FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference();
    }

    public static DatabaseService getInstance() {
        if (instance == null) {
            instance = new DatabaseService();
        }
        return instance;
    }

    private void writeData(@NotNull final String path, @NotNull final Object data, final @Nullable DatabaseCallback<Void> callback) {
        readData(path).setValue(data, (error, ref) -> {
            if (error != null) {
                if (callback != null) callback.onFailed(error.toException());
            } else {
                if (callback != null) callback.onCompleted(null);
            }
        });
    }

    private void deleteData(@NotNull final String path, @Nullable final DatabaseCallback<Void> callback) {
        readData(path).removeValue((error, ref) -> {
            if (error != null) {
                if (callback != null) callback.onFailed(error.toException());
            } else {
                if (callback != null) callback.onCompleted(null);
            }
        });
    }

    private DatabaseReference readData(@NotNull final String path) {
        return databaseReference.child(path);
    }

    private <T> void getData(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<T> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            T data = task.getResult().getValue(clazz);
            callback.onCompleted(data);
        });
    }

    private <T> void getDataList(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull final DatabaseCallback<List<T>> callback) {
        readData(path).get().addOnCompleteListener(task -> {
            if (!task.isSuccessful()) {
                Log.e(TAG, "Error getting data", task.getException());
                callback.onFailed(task.getException());
                return;
            }
            List<T> tList = new ArrayList<>();
            task.getResult().getChildren().forEach(dataSnapshot -> {
                T t = dataSnapshot.getValue(clazz);
                tList.add(t);
            });
            callback.onCompleted(tList);
        });
    }

    private String generateNewId(@NotNull final String path) {
        return databaseReference.child(path).push().getKey();
    }

    private <T> void runTransaction(@NotNull final String path, @NotNull final Class<T> clazz, @NotNull UnaryOperator<T> function, @NotNull final DatabaseCallback<T> callback) {
        readData(path).runTransaction(new Transaction.Handler() {
            @NonNull
            @Override
            public Transaction.Result doTransaction(@NonNull MutableData currentData) {
                T currentValue = currentData.getValue(clazz);
                currentValue = (currentValue == null) ? function.apply(null) : function.apply(currentValue);
                currentData.setValue(currentValue);
                return Transaction.success(currentData);
            }

            @Override
            public void onComplete(@Nullable DatabaseError error, boolean committed, @Nullable DataSnapshot currentData) {
                if (error != null) {
                    Log.e(TAG, "Transaction failed", error.toException());
                    callback.onFailed(error.toException());
                    return;
                }
                T result = currentData != null ? currentData.getValue(clazz) : null;
                callback.onCompleted(result);
            }
        });
    }

    // region User Section
    public void createNewUser(@NotNull final User user, @Nullable final DatabaseCallback<String> callback) {
        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        mAuth.createUserWithEmailAndPassword(user.getEmail(), user.getPassword())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Log.d(TAG, "createUserWithEmail:success");
                        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
                        user.setUserId(uid);
                        writeData(USERS_PATH + "/" + uid, user, new DatabaseCallback<Void>() {
                            @Override
                            public void onCompleted(Void v) {
                                if (callback != null) callback.onCompleted(uid);
                            }
                            @Override
                            public void onFailed(Exception e) {
                                if (callback != null) callback.onFailed(e);
                            }
                        });
                    } else {
                        Log.w(TAG, "createUserWithEmail:failure", task.getException());
                        if (callback != null) callback.onFailed(task.getException());
                    }
                });
    }

    public void getUser(@NotNull final String uid, @NotNull final DatabaseCallback<User> callback) {
        getData(USERS_PATH + "/" + uid, User.class, callback);
    }

    public void getUserList(@NotNull final DatabaseCallback<List<User>> callback) {
        getDataList(USERS_PATH, User.class, callback);
    }

    public void deleteUser(@NotNull final String uid, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(USERS_PATH + "/" + uid, callback);
    }

    public void getUserByEmailAndPassword(@NotNull final String email, @NotNull final String password, @NotNull final DatabaseCallback<User> callback) {
        readData(USERS_PATH).orderByChild("email").equalTo(email).get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting data", task.getException());
                        callback.onFailed(task.getException());
                        return;
                    }
                    if (task.getResult().getChildrenCount() == 0) {
                        callback.onFailed(new Exception("User not found"));
                        return;
                    }
                    for (DataSnapshot dataSnapshot : task.getResult().getChildren()) {
                        User user = dataSnapshot.getValue(User.class);
                        if (user == null || !Objects.equals(user.getPassword(), password)) {
                            callback.onFailed(new Exception("Invalid email or password"));
                            return;
                        }
                        callback.onCompleted(user);
                        return;
                    }
                });
    }

    public void checkIfEmailExists(@NotNull final String email, @NotNull final DatabaseCallback<Boolean> callback) {
        readData(USERS_PATH).orderByChild("email").equalTo(email).get()
                .addOnCompleteListener(task -> {
                    if (!task.isSuccessful()) {
                        Log.e(TAG, "Error getting data", task.getException());
                        callback.onFailed(task.getException());
                        return;
                    }
                    callback.onCompleted(task.getResult().getChildrenCount() > 0);
                });
    }

    public void updateUser(@NotNull final User user, @Nullable final DatabaseCallback<Void> callback) {
        runTransaction(USERS_PATH + "/" + user.getUserId(), User.class, currentUser -> user, new DatabaseCallback<User>() {
            @Override
            public void onCompleted(User object) {
                if (callback != null) callback.onCompleted(null);
            }
            @Override
            public void onFailed(Exception e) {
                if (callback != null) callback.onFailed(e);
            }
        });
    }
    // endregion User Section

    // region clothe section
    public void createNewClothe(@NotNull final Clothe clothe, @Nullable final DatabaseCallback<Void> callback) {
        writeData(CLOTHES_PATH + "/" + clothe.getItemId(), clothe, callback);
    }

    public void getClothe(@NotNull final String clotheId, @NotNull final DatabaseCallback<Clothe> callback) {
        getData(CLOTHES_PATH + "/" + clotheId, Clothe.class, callback);
    }

    public void getClotheList(@NotNull final DatabaseCallback<List<Clothe>> callback) {
        getDataList(CLOTHES_PATH, Clothe.class, callback);
    }

    public String generateClotheId() {
        return generateNewId(CLOTHES_PATH);
    }

    public void deleteClothe(@NotNull final String clotheId, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(CLOTHES_PATH + "/" + clotheId, callback);
    }

    // **פונקציה חדשה לקבלת רשימת מזהי בגדים של משתמש**
    public void getClothesIds(@NotNull final String userId, @NotNull final DatabaseCallback<List<String>> callback) {
        getClotheList(new DatabaseCallback<List<Clothe>>() {
            @Override
            public void onCompleted(List<Clothe> clothes) {
                List<String> ids = new ArrayList<>();
                for (Clothe clothe : clothes) {
                    if (Objects.equals(clothe.getItemId(), userId)) {
                        ids.add(clothe.getItemId());
                    }
                }
                callback.onCompleted(ids);
            }

            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    // endregion clothe section

    // region outfit section
    public void createNewOutfit(@NotNull final Outfit outfit, @Nullable final DatabaseCallback<Void> callback) {
        writeData(OUTFITS_PATH + "/" + outfit.getOutfitId(), outfit, callback);
    }

    public void getOutfit(@NotNull final String outfitId, @NotNull final DatabaseCallback<Outfit> callback) {
        getData(OUTFITS_PATH + "/" + outfitId, Outfit.class, callback);
    }

    public void getOutfitList(@NotNull final DatabaseCallback<List<Outfit>> callback) {
        getDataList(OUTFITS_PATH, Outfit.class, callback);
    }

    public void getUserOutfitList(@NotNull String uid, @NotNull final DatabaseCallback<List<Outfit>> callback) {
        getOutfitList(new DatabaseCallback<>() {
            @Override
            public void onCompleted(List<Outfit> outfits) {
                outfits.removeIf(outfit -> !Objects.equals(outfit.getOutfitId(), uid));
                callback.onCompleted(outfits);
            }
            @Override
            public void onFailed(Exception e) {
                callback.onFailed(e);
            }
        });
    }

    public String generateOutfitId() {
        return generateNewId(OUTFITS_PATH);
    }

    public void deleteOutfit(@NotNull final String outfitId, @Nullable final DatabaseCallback<Void> callback) {
        deleteData(OUTFITS_PATH + "/" + outfitId, callback);
    }
    // endregion outfit section
}
