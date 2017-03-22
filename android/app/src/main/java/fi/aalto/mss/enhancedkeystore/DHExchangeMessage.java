package fi.aalto.mss.enhancedkeystore;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by max on 22.03.17.
 */

public class DHExchangeMessage implements Parcelable, Serializable{
    private final int TYPE;
    private final int RESULT;
    private final DHGenParameters PARAMS;

    public DHExchangeMessage(int type, int result, DHGenParameters params) {
        this.TYPE = type;
        this.RESULT = result;
        this.PARAMS = params;
    }

    protected DHExchangeMessage(Parcel in) {
        TYPE = in.readInt();
        RESULT = in.readInt();
        PARAMS = in.readParcelable(DHGenParameters.class.getClassLoader());
    }

    public static final Creator<DHExchangeMessage> CREATOR = new Creator<DHExchangeMessage>() {
        @Override
        public DHExchangeMessage createFromParcel(Parcel in) {
            return new DHExchangeMessage(in);
        }

        @Override
        public DHExchangeMessage[] newArray(int size) {
            return new DHExchangeMessage[size];
        }
    };

    public int getType() {
        return this.TYPE;
    }

    public int getResult() {
        return this.RESULT;
    }

    public DHGenParameters getParams() {
        return this.PARAMS;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(TYPE);
        dest.writeInt(RESULT);
        dest.writeParcelable(PARAMS, flags);
    }
}
