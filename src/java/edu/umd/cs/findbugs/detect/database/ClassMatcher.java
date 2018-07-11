package edu.umd.cs.findbugs.detect.database;

import edu.umd.cs.findbugs.AnalysisCacheToRepositoryAdapter;
import edu.umd.cs.findbugs.detect.BadResourceCheck;
import org.apache.bcel.classfile.JavaClass;

import javax.annotation.Nonnull;
import java.util.logging.Logger;

/**
 * @author Peter Yu
 * @date 2018/6/20 20:32
 */
public class ClassMatcher {

    private static final AnalysisCacheToRepositoryAdapter adapter = new AnalysisCacheToRepositoryAdapter();

    private static final Logger LOGGER = Logger.getLogger(BadResourceCheck.class.getName());

    public static boolean matches(String className1, String className2){
        JavaClass aClass1 = adapter.findClass(className1);
        JavaClass aClass2 = adapter.findClass(className2);
        if(aClass1 == null||aClass2 == null){
            return false;
        }
        try {
            return aClass1.instanceOf(aClass2)||aClass2.instanceOf(aClass1);
        } catch (ClassNotFoundException e) {
            LOGGER.warning("Class not found:"+e.toString());
        }
        return false;
    }
}
