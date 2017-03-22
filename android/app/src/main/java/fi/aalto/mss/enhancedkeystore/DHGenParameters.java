package fi.aalto.mss.enhancedkeystore;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Serializable;

/**
 * Created by max on 22.03.17.
 */

public class DHGenParameters implements Parcelable, Serializable{

    private byte[] generator;
    private byte[] prime;
    private byte[] publicKey;

    public DHGenParameters(byte[] generator, byte[] prime, byte[] publicKey) {
        this.generator = generator;
        this.prime = prime;
        this.publicKey = publicKey;
    }

    protected DHGenParameters(Parcel in) {
        generator = in.createByteArray();
        prime = in.createByteArray();
        publicKey = in.createByteArray();
    }

    public byte[] getGenerator() {
        return generator;
    }

    public byte[] getPrime() {
        return prime;
    }

    public byte[] getPublicKey() {
        return publicKey;
    }


    public static final Creator<DHGenParameters> CREATOR = new Creator<DHGenParameters>() {
        @Override
        public DHGenParameters createFromParcel(Parcel in) {
            return new DHGenParameters(in);
        }

        @Override
        public DHGenParameters[] newArray(int size) {
            return new DHGenParameters[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {

        dest.writeByteArray(generator);
        dest.writeByteArray(prime);
        dest.writeByteArray(publicKey);
    }
}
