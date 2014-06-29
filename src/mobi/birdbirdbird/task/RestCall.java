
package mobi.birdbirdbird.task;

import android.os.AsyncTask;
import android.util.Log;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;

public class RestCall<T>
    extends AsyncTask<OAuthRequest, Void, T>
{
    public interface RS<T> {
        abstract void onRestResponse(T rs);
        abstract void onRestFailure(Exception e);
    }
    private RS<T> cb;
    private Exception excInfo;
    private ObjectMapper om;
    private JavaType jt;

    public RestCall(ObjectMapper om, RS handlers) {
        cb = handlers;
        this.om = om;
    }

    public RestCall<T> setClass(Class c) {
        jt = om.getTypeFactory().constructType(c);
        return this;
    }

    protected T doInBackground(OAuthRequest... request) {
        T converted = null;
        excInfo = null;
        Log.d("DEBUG", "RestCall: " + request);
        try {
            Response rs = request[0].send();
            if (rs.isSuccessful()) {
                Log.d("DEBUG", "Success!");
                converted = (T) om.readValue(rs.getStream(), jt);
                Log.d("DEBUG", "Converted!");
            }
            else {
                throw new Exception
                    ("HTTP Error " + rs.getCode() + ": " +
                     rs.getMessage());
            }
        }
        catch (Exception e) {
            Log.d("DEBUG", "Erp.  Exception " + e);
            excInfo = e;
        }
        Log.d("DEBUG", "Returning!  Val = " + converted);
        return converted;
    }

    protected void onPostExecute(T result) {
        Log.d("DEBUG", "RestCall.onPostExecute(" + result + ")");
        if (result == null) {
            cb.onRestFailure(excInfo);
        }
        else {
            cb.onRestResponse(result);
        }
    }
}
