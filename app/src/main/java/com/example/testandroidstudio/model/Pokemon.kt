package com.example.testandroidstudio.model

import android.os.Parcel
import android.os.Parcelable

data class Pokemon(
    val id: Int,
    val name: String,
    val types: List<String>,
    val imageUrl: String,
    val description: String
) : Parcelable {
override fun describeContents(): Int {
    return 0
}

override fun writeToParcel(parcel: Parcel, flags: Int) {
    parcel.writeInt(id)
    parcel.writeString(name)
    parcel.writeStringList(types)
    parcel.writeString(imageUrl)
    parcel.writeString(description)
}

companion object CREATOR : Parcelable.Creator<Pokemon> {
    override fun createFromParcel(parcel: Parcel): Pokemon {
        return Pokemon(
            parcel.readInt(),
            parcel.readString() ?: "",
            parcel.createStringArrayList() ?: listOf(),
            parcel.readString() ?: "",
            parcel.readString() ?: ""
        )
    }

    override fun newArray(size: Int): Array<Pokemon?> {
        return arrayOfNulls(size)
    }
}
}