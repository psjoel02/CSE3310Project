package com.example.cse3310project;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.widget.Toast;
import android.util.Log;

import androidx.annotation.Nullable;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class AnimalDataHelper extends SQLiteOpenHelper {

    public static final String ANIMAL_TABLE = "ANIMAL_TABLE";
    public static final String COLUMN_ANIMAL_TYPE = "ANIMAL_TYPE";
    public static final String COLUMN_ANIMAL_BREED = "ANIMAL_BREED";
    public static final String COLUMN_DESCRIPTION = "DESCRIPTION";
    public static final String COLUMN_ID = "ID";

    /*
    private static String dbName = "animal.db";
    String dbPath;
    private static int DB_VERSION = 2;
    SQLiteDatabase dataBase;
    private final Context dataContext;
*/

    public AnimalDataHelper(Context context, String name) {
        super(context, "animal1.db", null, 1);

    }
    @Override
    public void onCreate(SQLiteDatabase db) {
        /*
        db.execSQL("create Table ANIMAL_TABLE(id INTEGER primary key, type TEXT, breed TEXT, description TEXT)");
        db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_ID, 1);
        cv.put(COLUMN_ANIMAL_TYPE, "Dog");
        cv.put(COLUMN_ANIMAL_BREED, "Labrador");
        cv.put(COLUMN_DESCRIPTION, "Flavor text");
        long insert = db.insert(ANIMAL_TABLE, null, cv);
*/
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }

/*
    private boolean checkDatabase(){
        try{
            final String path = dbPath + dbName;
            final File file = new File(path);
            if(file.exists())
                return true;
            else
                return false;
        }catch(SQLiteException e){
            e.printStackTrace();
            return false;
        }
    }
    public void copyDatabase() throws IOException{
        try {
            InputStream input = dataContext.getAssets().open(dbName);
            String outputName = dbPath + dbName;
            OutputStream output = new FileOutputStream(outputName);
            byte[] buffer = new byte[1024];

            int len;
            while ((len = input.read(buffer)) > 0) {
                output.write(buffer, 0, len);
            }
            output.flush();
            input.close();
            output.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void createDataBase() throws IOException {
        boolean databaseExist = checkDatabase();
        if(!databaseExist){
            this.getReadableDatabase();
            this.close();
            try{
                copyDatabase();
            }catch(IOException mIOException){
                mIOException.printStackTrace();
                throw new Error("Error Database already exists");
            } finally{
                this.close();
            }
        }
    }

    @Override
    public synchronized void close(){
        if(dataBase != null)
            dataBase.close();
        SQLiteDatabase.releaseMemory();
        super.close();
    }

    public String loadHandler(){
        try{
            createDataBase();
        }catch(IOException e){
            e.printStackTrace();
        }
        String result = "";
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor c = db.rawQuery("SELECT * FROM Animal", null);
        while(c.moveToNext()){
            String type = c.getString(1);
            String breed = c.getString(2);
            result += type + breed + System.getProperty("line.separator");
        }
        return result;
    }


    public String getInfo(String breed){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "SELECT Type FROM AnimalTable WHERE  Breed = " + breed;

        Cursor cursor = db.rawQuery(queryString, null);
        StringBuffer buffer = new StringBuffer();
        while(cursor.moveToNext()){
            int index1 = cursor.getColumnIndex("Type");
            String type = cursor.getString(index1);
            buffer.append(type + "\n");
        }
        return buffer.toString();
    }

    */
/*
    public boolean addOne(AnimalModel animalModel){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();

        cv.put(COLUMN_ANIMAL_TYPE, animalModel.getaType());
        cv.put(COLUMN_ANIMAL_BREED, animalModel.getaBreed());
        cv.put(COLUMN_DESCRIPTION, animalModel.getaDescription());

        long insert = db.insert(ANIMAL_TABLE, null, cv);
        return insert != -1;
    }

    public void deleteOne(AnimalModel animalModel){
        SQLiteDatabase db = this.getWritableDatabase();
        String queryString = "DELETE FROM " + ANIMAL_TABLE + " WHERE " + COLUMN_ID + " = " + animalModel.getId();

        @SuppressLint("Recycle") Cursor cursor = db.rawQuery(queryString, null);

        cursor.moveToFirst();
    }

    public List<AnimalModel> getAllAnimals(){

        List<AnimalModel> animalList = new ArrayList<>();

        String queryString = "SELECT * FROM " + ANIMAL_TABLE;

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(queryString, null);

        if(cursor.moveToFirst()){
            do{
                int animalID = cursor.getInt(0);
                String animalType = cursor.getString(1);
                String animalBreed = cursor.getString(2);
                String animalDescription = cursor.getString(3);

                AnimalModel newAnimal = new AnimalModel(animalID, animalType, animalBreed, animalDescription);
                animalList.add(newAnimal);

            } while(cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return animalList;
    }
 */
}