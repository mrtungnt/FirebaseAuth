package com.example.firebaseauth.data

import androidx.datastore.core.CorruptionException
import androidx.datastore.core.Serializer
import com.example.firebaseauth.SelectedCountry
import com.google.protobuf.InvalidProtocolBufferException
import java.io.InputStream
import java.io.OutputStream

object SelectedCountrySerializer : Serializer<SelectedCountry> {
    override val defaultValue: SelectedCountry = SelectedCountry.getDefaultInstance()
    override suspend fun readFrom(input: InputStream): SelectedCountry {
        try {
            return SelectedCountry.parseFrom(input)
        } catch (exception: InvalidProtocolBufferException) {
            throw CorruptionException("Cannot read proto.", exception)
        }
    }

    override suspend fun writeTo(t: SelectedCountry, output: OutputStream) = t.writeTo(output)
}
