// Callback.aidl
package com.android.NanoServer;
// Declare any non-default types here with import statements
interface INanoMethodCallback
{
    void VoiceKeyEvent(in int action);

    void VoiceDataEvent(in byte[] data,in int datalen);

}
