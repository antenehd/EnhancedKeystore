package fi.aalto.mss.enhancedkeystore;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by max on 22.03.17.
 */

public class DHExchangeResult implements Parcelable {

    private byte[] keyID;
    private DHGenParameters dhParams;

    public DHExchangeResult(byte[] keyID) {
        this.keyID = keyID;
        this.dhParams = null;
    }

    public DHExchangeResult(byte[] keyID, DHGenParameters dhParams) {
        this.keyID = keyID;
        this.dhParams = dhParams;
    }

    protected DHExchangeResult(Parcel in) {
        keyID = in.createByteArray();
        dhParams = in.readParcelable(DHGenParameters.class.getClassLoader());
    }

    public static final Creator<DHExchangeResult> CREATOR = new Creator<DHExchangeResult>() {
        @Override
        public DHExchangeResult createFromParcel(Parcel in) {
            return new DHExchangeResult(in);
        }

        @Override
        public DHExchangeResult[] newArray(int size) {
            return new DHExchangeResult[size];
        }
    };

    public byte[] getKeyID() {
        return keyID;
    }

    public DHGenParameters getDhParams() {
        return this.dhParams;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeByteArray(keyID);
        dest.writeParcelable(dhParams, flags);
    }
}
