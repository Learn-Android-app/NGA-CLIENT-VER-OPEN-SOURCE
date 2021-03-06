package sp.phone.task;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.zip.GZIPInputStream;

import gov.anzong.androidnga.R;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import sp.phone.bean.MessageDetialInfo;
import sp.phone.interfaces.OnAuthcodeLoadFinishedListener;
import sp.phone.interfaces.OnMessageDetialLoadFinishedListener;
import sp.phone.utils.ActivityUtil;
import sp.phone.utils.HttpUtil;
import sp.phone.utils.ImageUtil;
import sp.phone.utils.MessageUtil;
import sp.phone.utils.PhoneConfiguration;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;
import sp.phone.utils.StringUtil;

public class AccountAuthcodeImageReloadTask extends
		AsyncTask<String, Integer, Bitmap> {
	private final static String TAG = AccountAuthcodeImageReloadTask.class
			.getSimpleName();
	private final Context context;
	private String error;
	@SuppressWarnings("unused")
	private String table;
	OnAuthcodeLoadFinishedListener notifier;
	String authcode;

	String authcodeUrl = "http://account.178.com/q_vcode.php?_act=gen_reg";

	public AccountAuthcodeImageReloadTask(Context context,
			OnAuthcodeLoadFinishedListener notifier) {
		super();
		this.context = context;
		this.notifier = notifier;
	}

	@Override
	protected Bitmap doInBackground(String... params) {

		URL url = null;
		Bitmap bitmap;
		try {
			url = new URL(authcodeUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		try {
			HttpURLConnection conn = (HttpURLConnection) url
					.openConnection();
			conn.setRequestProperty("Accept-Charset", "GBK");
			if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.FROYO) {
		        System.setProperty("http.keepAlive", "false");
		    }else{
				conn.setRequestProperty("Connection", "close");
		    }
			conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
			conn.setConnectTimeout(5000);
			conn.setReadTimeout(10000);
			conn.connect();
            if(conn.getResponseCode() == 200){
				String cookieVal = null;
				String key = null;
				for (int i = 1; (key = conn.getHeaderFieldKey(i)) != null; i++) {
					if (key.equalsIgnoreCase("set-cookie")) {
						cookieVal = conn.getHeaderField(i);
						cookieVal = cookieVal.substring(0,
								cookieVal.indexOf(';'));
						Log.i(TAG,cookieVal);
						if (cookieVal.indexOf("reg_vcode=") == 0 && cookieVal.indexOf("deleted")<0)
							authcode = cookieVal.substring(10);
					}
				}
    			conn.connect();
    			InputStream is = conn.getInputStream();
    			bitmap = BitmapFactory.decodeStream(is);
    			is.close();
    			if(!StringUtil.isEmpty(authcode)){
    				return bitmap;
    			}
            }
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected void onPostExecute(Bitmap result) {
		if(result!=null){
			notifier.authcodefinishLoad(result, authcode);
		}else{
			notifier.authcodefinishLoadError();
		}
		super.onPostExecute(result);
	}

	@Override
	protected void onCancelled() {
		super.onCancelled();
	}

}
