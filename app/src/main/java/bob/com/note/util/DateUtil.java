package bob.com.note.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * bob.com.note.util
 * Created by BOB on 2017/3/6.
 * 描述：时间工具类，用于获取当前时间写入数据库
 * 博客园：http://www.cnblogs.com/ghylzwsb/
 * 个人网站：www.wensibo.top
 */

public class DateUtil {

    public static String formatDateTime() {
        return new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss").format(new Date(System.currentTimeMillis()));
    }

}
