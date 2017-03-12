package com.buyhatke.chatfirebaseadminlibrary;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by devdoot on 6/6/16.
 */
public class ImageHandler {

    public static void imageTransform(File image){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(image.getPath(),options);
        options.inSampleSize=options.outWidth/480;
        options.inJustDecodeBounds=false;
        Bitmap bitmap=BitmapFactory.decodeFile(image.getPath(),options);

        try {
            FileOutputStream fs=new FileOutputStream(image);

            bitmap.compress(Bitmap.CompressFormat.JPEG,50,fs);
            bitmap.recycle();
        } catch (FileNotFoundException e) {
            bitmap.recycle();
            e.printStackTrace();
        }

    }
    public static File createImageFile(Context context)throws IOException {
        String timestamp=new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        File imageFile=null;
        if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.M){
            imageFile=
                    new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+"BuyHatkeImages");
            if(!imageFile.exists()) {imageFile.mkdirs();
                System.out.println(imageFile.exists()+"///////////////////////////////////////////////////////////////////////");
            }
        }else{
            imageFile=context.getFilesDir();
        }
        File image;
        image= File.createTempFile("User_"+timestamp+"-0",".jpg",imageFile);
        if(imageFile==null)System.out.println("SSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSSS");
        return image;
    }
    public static File createImageFile(String name,Context context)throws IOException{
        File imageFile=null;
        if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.M){
            imageFile=
                new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+"BuyHatkeImages");
        if(!imageFile.exists()) imageFile.mkdirs();}
        else {
            imageFile=context.getFilesDir();
        }
        File image;
        image= File.createTempFile(name.substring(0,name.indexOf('-'))+"-0",name.substring(name.lastIndexOf('.')),imageFile);
        return image;
    }
    public static Uri ImageExists(String name,Context context){
        File imageFile=null;
        if(Build.VERSION.SDK_INT<=Build.VERSION_CODES.M){
            imageFile= new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)+File.separator+"BuyHatkeImages");
        }
        else{
            imageFile=context.getFilesDir();
        }
        if(name.indexOf('-')==-1)return null;
        try{
            File [] matches=findFilesForId(imageFile,name.substring(0,name.indexOf('-')));
            if(matches.length>0)return Uri.fromFile(matches[0]);
            else return null;}
        catch (StringIndexOutOfBoundsException e){
            return null;
        }
        catch (NullPointerException e){
            return null;
        }
    }
    private static File[] findFilesForId(File dir, final String id) {
        return dir.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                if(pathname.getName().matches(id+"-(.*)")){
                    return  true;
                }else {
                    return false;
                }
            }
        });
    }
    public static void imageRotate(Uri imageUri, Matrix matrix){
        Bitmap bmp=BitmapFactory.decodeFile(imageUri.getPath());
        bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
        try {
            FileOutputStream fs=new FileOutputStream(new File(imageUri.getPath()));
            bmp.compress(Bitmap.CompressFormat.JPEG,100,fs);
            bmp.recycle();
        } catch (FileNotFoundException e) {
            bmp.recycle();
            e.printStackTrace();
        }
    }
    public static Uri copyAndTransformImage(final Uri imageUri, final Context context){
        BitmapFactory.Options options=new BitmapFactory.Options();
        options.inJustDecodeBounds=true;
        BitmapFactory.decodeFile(getPath(imageUri,context),options);
        options.inSampleSize=options.outWidth/480;
        options.inJustDecodeBounds=false;
        Bitmap bitmap=BitmapFactory.decodeFile(getPath(imageUri,context),options);
        try {
            File image= ImageHandler.createImageFile(context);
            FileOutputStream fileOutputStream=new FileOutputStream(image);

            bitmap.compress(Bitmap.CompressFormat.JPEG,50,fileOutputStream);
            //mMessenger.writeToAdmin(Uri.fromFile(image));
            bitmap.recycle();
            return Uri.fromFile(image);
        } catch (IOException e) {
            bitmap.recycle();
            e.printStackTrace();
            return null;
        }
    }

    private static String getPath(final Uri uri,final Context context) {
        // just some safety built in
        if( uri == null ) {

            return null;
        }
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = context.getContentResolver().query(uri, projection, null, null, null);
        if( cursor != null ){
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }



}
