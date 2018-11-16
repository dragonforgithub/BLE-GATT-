#ifndef __JB_NANOGATT__H__
#define __JB_NANOGATT__H__

#ifdef __cplusplus
extern "C" { 
#endif

/*基本数据类型*/
typedef unsigned char		J_U8; 			/**< 8位无符号数*/
typedef unsigned short 		J_U16;			/**< 16位无符号数*/
typedef unsigned long		J_U32;			/**< 32位无符号数*/
typedef char				J_S8;			/**< 8位有符号数*/
typedef short				J_S16;  		/**< 8位有符号数*/
typedef long				J_S32;  		/**< 8位有符号数*/
typedef float				J_Float;		/**< 32位浮点型*/
typedef double				J_Double;		/**< 64位双精度型*/
typedef void*				J_Ptr;			/**< 通用指针*/
typedef int					J_Int;			/**< 整数*/
typedef unsigned int		J_UInt;			/**< 无符号整数*/
typedef long int			J_Size;			/**< 空间大小*/
typedef J_U8				J_BOOL;			/** @brief 布尔型*/

typedef enum{
	CLASS_BLUETOOTH,
	CLASS_DONGLE,
	CLASS_UNKOWN,
}EM_DEVTYPE;

typedef J_Int (*AppCallback) (J_U8* data,J_U32 datalen);
typedef void (*RCNotificationCB)(EM_DEVTYPE dev_type);
typedef void (*VoiceDataCb)(char* pBuf,int length);
typedef void (*VoiceKeyCb)(int action);
typedef void (*WriteCb)(char* pBuf,int length);

/*-----------------------------------------------------------------------------
Function Name:	nano_open
Input		:	
Output		:	
Return 		:	0		成功 
				其它   失败
Describe		:	卡拉OK中间初始化 
-------------------------------------------------------------------------------*/
int nano_open(AppCallback cb);

/*-----------------------------------------------------------------------------
Function Name:	nano_close
Input		:	
Output		:	
Return 		:	0		成功 
				其它   失败
Describe		:	卡拉OK中间去初始化
-------------------------------------------------------------------------------*/
int nano_close(void);

/*-----------------------------------------------------------------------------
Function Name:	nano_appProcData
Input		:		
Output		:	
Return 		:	
Describe	:	处理压缩数据
-------------------------------------------------------------------------------*/	
int nano_appProcData(J_U8* dataIn,J_U8 datalen);

/*-----------------------------------------------------------------------------
Function Name:	 
Input		:		
Output		:	
Return 		:	
Describe	:	回调函数
-------------------------------------------------------------------------------*/	
void nano_RegisterDataReceivedCb(VoiceDataCb func);
void nano_RegisterVoiceKeyCb(VoiceKeyCb func);
void nano_RegisterWriteCmdCb(WriteCb func);
void nano_RegisterWriteReqCb(WriteCb func);


#ifdef __cplusplus
}
#endif


#endif
