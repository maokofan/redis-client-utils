package maoko.redis.utils.except;



/**
 * 错误编号
 * 
 * @author fanpei
 *
 */

/**
 * @author fanpei
 *
 */
public enum ErrorCode {
	SuccesNoError(0x00000), // 无错误,正确

	/*
	 * 系统级别错误[0x00001~0x11000)
	 */
	// 未知错误
	UnknownError(0x00001),
	// 系统错误
	SystemError(0x00002),
	// 数据库操作错误
	DbOperateError(0x00003),
	// 网络连接错误
	NetConectExp(0x00004),
	// 任务不存在
	TaskNoExist(0x00005),

	UNKOWN_CMD(0x00006), // 未知命令

	// 文件或文件夹不存在
	FILE_OR_DIR_NOEXSIT(0x00007), // 文件或目录不存在
	OPT_TIME_OUT(0x00008), // 操作超时
	OBJECT_ALREADY_EXIST(0x0009), // 对象已存在
	NO_TASK_CREATE(0x00010), // / 没有任务建立
	TD_INTERRUPTED(0x00011), // 进程被打断
	WRONG_LOGIC_OPT(0x00012), // 错误的逻辑处理
	SOURCE_LOCKED(0x00013), // 资源被占用
	WRONG_REQ_PARAM(0x00014), // 错误的请求参数
	DIR_HASCHILD(0x00015), // 文件夾不為空，具有子節點
	WRONG_DATA(0x00016), TaskComplete(0x00017), // 任务已结束

	// 权限受限[0x10000~0x10010)
	PermissionDenied(0x10000),
	// 权限生成错误
	Permission_create_Error(0x10001),
	// 权限校验错误
	Permission_check_Error(0x10002),

	/*
	 * 2. 报文模块错误[20000,21000)
	 */
	// xml数据格式错误或节点值不在正确的范围内
	XmlFormatterError(0x20000),
	// 数据报文,xml为空
	XmlNull(0x20001),
	// Xml某个节点不存在
	XmlNodeNull(0x20002),
	// Xml某个属性不存在
	XmlAttributeNull(0x20003),
	// 不正确组装的报文
	WrongDataGram(0x20004),
	// 不正确的文件数据包
	WrongFileData(0x20005),
	// 组装反馈Xml失败
	XML_RESP_ERROR(0x20006),

	/*
	 * 3.数据值错误[21000,~21999]
	 */
	// 枚举值超出范围
	EnumOutOfRange(0x21000),
	// 对象为空
	ObjectNullError(0x21001),
	// 不正确的数值类型
	WRONG_VALUE_TYPE(0x21002),
	// 数据值不在正确的范围内
	OUT_OF_VALUE_RANGE(0x21003),


	// 缓存错误[0x23000-0x23999]
	REDIS_NOT_INIT(0x23000), // REDIS未初始化
	RedisCacheError(0x23001), REDIS_LOCK_ERORR(0x23002), // 加锁失败
	WAIT_DATA(0x23003), // 数据不一致，等待数据
	REDIS_OOM(0x23004); // 缓存达到上限

	private int nCode;

	private ErrorCode(int _nCode) {
		this.nCode = _nCode;
	}

	public int GetErrorNum() {
		return nCode;
	}

	public static ErrorCode geErrorCode(int value) throws CusException {
		switch (value) {
		case 0x00000:
			return SuccesNoError;
		case 0x00001:
			return UnknownError;
		case 0x00002:
			return SystemError;
		case 0x00003:
			return DbOperateError;
		case 0x00004:
			return NetConectExp;
		case 0x00005:
			return TaskNoExist;
		case 0x00006:
			return UNKOWN_CMD;
		case 0x00009:
			return OBJECT_ALREADY_EXIST;
		case 0x00010:
			return NO_TASK_CREATE;
		case 0x00011:
			return TD_INTERRUPTED;
		case 0x00012:
			return WRONG_LOGIC_OPT;
		case 0x00013:
			return SOURCE_LOCKED;
		case 0x00014:
			return WRONG_REQ_PARAM;
		case 0x00015:
			return DIR_HASCHILD;
		case 0x00016:
			return WRONG_DATA;

		case 0x10000:
			return PermissionDenied;
		case 0x10001:
			return Permission_create_Error;
		case 0x10002:
			return Permission_check_Error;

		case 0x20000:
			return XmlFormatterError;
		case 0x20001:
			return XmlNull;
		case 0x20002:
			return XmlNodeNull;
		case 0x20003:
			return XmlAttributeNull;
		case 0x20004:
			return WrongDataGram;
		case 0x20005:
			return WrongFileData;

		case 0x21000:
			return EnumOutOfRange;
		case 0x21001:
			return ObjectNullError;
		case 0x21002:
			return WRONG_VALUE_TYPE;

		case 0x23000:
			return REDIS_NOT_INIT;
		case 0x23001:
			return RedisCacheError;
		case 0x23002:
			return REDIS_LOCK_ERORR;
		case 0x23003:
			return WAIT_DATA;

		case 0x23004:
			return REDIS_OOM;

		default:
			throw new CusException(EnumOutOfRange, "错误编码超出系统定义类型");
		}

	}
}
