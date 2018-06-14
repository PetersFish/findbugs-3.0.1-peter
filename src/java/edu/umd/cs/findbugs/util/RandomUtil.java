package edu.umd.cs.findbugs.util;

import sun.security.provider.MD5;

import java.util.UUID;

/**
 * @author Peter Yu
 * @date 2018/6/7 16:10
 */
public class RandomUtil {

    private RandomUtil(){}

    public static String getRandomId(){
        return UUID.randomUUID().toString().replace("-","");
    }

    public static void main(String[] args) {
        String a = "Aa";
        String b = "BB";
        System.out.println(a.hashCode());
        System.out.println(b.hashCode());
        String c = RandomUtil.getRandomId();
        String d = RandomUtil.getRandomId();
        System.out.println(c.hashCode());
        System.out.println(d.hashCode());

    }
}
