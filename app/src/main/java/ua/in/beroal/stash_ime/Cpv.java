package ua.in.beroal.stash_ime;

import android.content.Intent;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.ibm.icu.lang.UCharacter;
import com.ibm.icu.lang.UProperty;

/**
 * Immutable.
 */
public final class Cpv implements Parcelable {
    public static final String CONTAINS_FIELD = "contains";
    public static final String PROPERTY_ID_FIELD = "property_id";
    public static final String VALUE_ID_FIELD = "value_id";
    private final int propertyId;
    private final int valueId;

    public Cpv(int propertyId, int valueId) {
        this.propertyId = propertyId;
        this.valueId = valueId;
    }

    public String getPropertyName(int nameChoice) {
        return UCharacter.getPropertyName(getPropertyId(), nameChoice);
    }

    public String getValueName(int nameChoice) {
        return UCharacter.getPropertyValueName(getPropertyId(), getValueId(), nameChoice);
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

    public static void writeToIntent(@NonNull Intent intent, @Nullable Cpv value) {
        if (value == null) {
            intent.putExtra(CONTAINS_FIELD, false);
        } else {
            intent.putExtra(CONTAINS_FIELD, true);
            intent.putExtra(PROPERTY_ID_FIELD, value.propertyId);
            intent.putExtra(VALUE_ID_FIELD, value.valueId);
        }

    }

    public static Cpv readFromIntent(@NonNull Intent intent) {
        if (!intent.getBooleanExtra(CONTAINS_FIELD, false)) {
            return null;
        } else {
            if (!(intent.hasExtra(PROPERTY_ID_FIELD) && intent.hasExtra(VALUE_ID_FIELD))) {
                throw new IllegalArgumentException(
                        "An intent does not contain a character property value.");
            } else {
                Cpv cpv = new Cpv(intent.getIntExtra(PROPERTY_ID_FIELD, -1),
                        intent.getIntExtra(VALUE_ID_FIELD, -1));
                try {
                    cpv.getValueName(UProperty.NameChoice.LONG);
                } catch (IllegalArgumentException e) {
                    cpv = null;
                }
                return cpv;
            }
        }
    }
}
