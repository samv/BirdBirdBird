
package mobi.birdbirdbird.task;

import android.os.AsyncTask;
import android.util.Log;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.scribe.model.OAuthRequest;
import org.scribe.model.Response;

public class RestListCall<T>
    extends AsyncTask<OAuthRequest, Void, List<T>>
{
    public interface RS<T> {
        abstract void onRestResponse(List<T> rs);
        abstract void onRestFailure(Exception e);
    }

    private RS<T> cb;
    private Exception excInfo;
    private ObjectMapper om;
    private JavaType jt;

    public RestListCall(ObjectMapper om, RS handlers) {
        cb = handlers;
        this.om = om;
    }

    public RestListCall<T> setItemClass(Class c) {
        jt = om.getTypeFactory().constructCollectionType(List.class, c);
        return this;
    }

    protected List<T> doInBackground(OAuthRequest... request) {
        List<T> converted = null;
        excInfo = null;
        Log.d("DEBUG", "RestListCall: " + request);
        try {
            Response rs = request[0].send();
            if (rs.isSuccessful()) {
                Log.d("DEBUG", "Success!");
                converted = (List<T>) om.readValue(rs.getStream(), jt);
                Log.d("DEBUG", "Converted!  count = " + converted.size());
            }
            else {
                throw new Exception
                    ("HTTP Error " + rs.getCode() + ": " +
                     rs.getMessage());
            }
        }
        catch (Exception e) {
            excInfo = e;
            Log.d("DEBUG", "Erp.  Exception " + e);
        }
        return converted;
    }

    protected void onPostExecute(List<T> result) {
        //Log.d("DEBUG", "RestListCall.onPostExecute(" + result + ")");
        if (result == null) {
           cb.onRestFailure(excInfo);
        }
        else {
            cb.onRestResponse(result);
        }
    }
}
