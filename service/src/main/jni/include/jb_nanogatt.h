#ifndef __JB_NANOGATT__H__
#define __JB_NANOGATT__H__

#ifdef __cplusplus
extern "C" { 
#endif

/*������������*/
typedef unsigned char		J_U8; 			/**< 8λ�޷�����*/
typedef unsigned short 		J_U16;			/**< 16λ�޷�����*/
typedef unsigned long		J_U32;			/**< 32λ�޷�����*/
typedef char				J_S8;			/**< 8λ�з�����*/
typedef short				J_S16;  		/**< 8λ�з�����*/
typedef long				J_S32;  		/**< 8λ�з�����*/
typedef float				J_Float;		/**< 32λ������*/
typedef double				J_Double;		/**< 64λ˫������*/
typedef void*				J_Ptr;			/**< ͨ��ָ��*/
typedef int					J_Int;			/**< ����*/
typedef unsigned int		J_UInt;			/**< �޷�������*/
typedef long int			J_Size;			/**< �ռ��С*/
typedef J_U8				J_BOOL;			/** @brief ������*/

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
Return 		:	0		�ɹ� 
				����   ʧ��
Describe		:	����OK�м��ʼ�� 
-------------------------------------------------------------------------------*/
int nano_open(AppCallback cb);

/*-----------------------------------------------------------------------------
Function Name:	nano_close
Input		:	
Output		:	
Return 		:	0		�ɹ� 
				����   ʧ��
Describe		:	����OK�м�ȥ��ʼ��
-------------------------------------------------------------------------------*/
int nano_close(void);

/*-----------------------------------------------------------------------------
Function Name:	nano_appProcData
Input		:		
Output		:	
Return 		:	
Describe	:	����ѹ������
-------------------------------------------------------------------------------*/	
int nano_appProcData(J_U8* dataIn,J_U8 datalen);

/*-----------------------------------------------------------------------------
Function Name:	 
Input		:		
Output		:	
Return 		:	
Describe	:	�ص�����
-------------------------------------------------------------------------------*/	
void nano_RegisterDataReceivedCb(VoiceDataCb func);
void nano_RegisterVoiceKeyCb(VoiceKeyCb func);
void nano_RegisterWriteCmdCb(WriteCb func);
void nano_RegisterWriteReqCb(WriteCb func);


#ifdef __cplusplus
}
#endif


#endif
