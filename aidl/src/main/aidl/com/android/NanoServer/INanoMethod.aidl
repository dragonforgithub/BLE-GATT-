// IMyAidlInterface.aidl
package com.android.NanoServer;
import com.android.NanoServer.INanoMethodCallback;
// Declare any non-default types here with import statements

interface INanoMethod {
    /**
     * 注册INanoVoiceCallback相关回调接口.
     *
     * @param INanoVoiceCallback 回调接口
     */
    void registerCallBack( INanoMethodCallback callback);
    /**
     * 解除INanoVoiceCallback相关回调接口.
     *
     * @param INanoVoiceCallback 回调接口
     */
    void unregisterCallBack(INanoMethodCallback callback);
    /**
     * 保存Activity对象.
     */
    void registerActivity();

}
