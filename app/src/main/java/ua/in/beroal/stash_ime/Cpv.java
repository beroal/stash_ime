package ua.in.beroal.util.unicode;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;

public class Cpv implements Parcelable {
    public static final String CONTAINS_FIELD = "contains";
    public static final String PROPERTY_ID_FIELD = "property_id";
    public static final String VALUE_ID_FIELD = "value_id";
    private int propertyId;
    private int valueId;

    public Cpv(int propertyId, int valueId) {
        this.propertyId = propertyId;
        this.valueId = valueId;
    }

    public int getPropertyId() {
        return propertyId;
    }

    public int getValueId() {
        return valueId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(propertyId);
        dest.writeInt(valueId);
    }

    public static final Parcelable.Creator<Cpv> CREATOR
            = new Parcelable.Creator<Cpv>() {
        public Cpv createFromParcel(Parcel in) {
            return new Cpv(in.readInt(), in.readInt());
        }

        public Cpv[] newArray(int size) {
            return new Cpv[size];
        }
    };

    public static void writeToIntent(Intent intent, Cpv value) {
        if (value == null) {
            intent.putExtra(CONTAINS_FIELD, false);
        } else {
            intent.putExtra(CONTAINS_FIELD, true);
            intent.putExtra(PROPERTY_ID_FIELD, value.propertyId);
            intent.putExtra(VALUE_ID_FIELD, value.valueId);
        }

    }

    public static Cpv readFromIntent(Intent intent) {
        if (!intent.getBooleanExtra(CONTAINS_FIELD, false)) {
            return null;
        } else {
            return new Cpv(intent.getIntExtra(PROPERTY_ID_FIELD, -1),
                    intent.getIntExtra(VALUE_ID_FIELD, -1));
        }
    }
}
