package com.davemorrissey.labs.subscaleview.sample;

import android.content.Context;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by Elbit on 8/13/2017.
 */

public class ResorcesCopier {
    Context mContext;
    final String TAG = getClass().getName();

    ResorcesCopier(Context context){
        mContext = context;
    }



    public void copyResources(int resId, String filename, String outputPath, String suffix ){
        Log.i(TAG, "Setup::copyResources");

        InputStream in = mContext.getResources().openRawResource(resId);
//        String suffix = ".xls";
        filename += suffix;
        File f = new File(filename);

        if(!f.exists()){
            try {
                OutputStream out = new FileOutputStream(new File(outputPath, filename));
                byte[] buffer = new byte[1024];
                int len;
                while((len = in.read(buffer, 0, buffer.length)) != -1){
                    out.write(buffer, 0, len);
                }
                in.close();
                out.close();
            } catch (FileNotFoundException e) {
                Log.i(TAG, "Setup::copyResources - "+e.getMessage());
            } catch (IOException e) {
                Log.i(TAG, "Setup::copyResources - "+e.getMessage());
            }
        }
    }

}


