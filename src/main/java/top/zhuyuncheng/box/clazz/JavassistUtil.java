package top.zhuyuncheng.box.clazz;

import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.Sets;

import javassist.ClassClassPath;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.NotFoundException;
import javassist.bytecode.AnnotationsAttribute;
import javassist.bytecode.ClassFile;
import javassist.bytecode.FieldInfo;
import javassist.bytecode.MethodInfo;
import javassist.bytecode.annotation.Annotation;
import lombok.extern.slf4j.Slf4j;

/**
 * 通过javassist库获取类的依赖
 */
@Slf4j
public final class JavassistUtil {

    /**
     * 获取类下所有的依赖（注解、父类、接口、字段、方法声明、代码）
     */
    public static Set<String> getAllDependency(String classPath) throws NotFoundException, ClassNotFoundException {
        final Set<String> set = Sets.newHashSet();
        set.addAll(getClassAnnotations(classPath));
        set.addAll(getSuperClass(classPath));
        set.addAll(getInterface(classPath));
        set.addAll(getFieldsType(classPath));
        set.addAll(getMethodSignatureClass(classPath));
        set.addAll(getClassRelyClass(classPath));
        return set;
    }

    /**
     * 获取类注解
     */
    public static Set<String> getClassAnnotations(String classPath) throws NotFoundException {
        Set<String> set = Sets.newHashSet();
        CtClass ctClass = getCtClass(classPath);
        ClassFile classFile = ctClass.getClassFile();

        AnnotationsAttribute attribute = (AnnotationsAttribute) classFile.getAttribute(AnnotationsAttribute.visibleTag);
        if (attribute != null) {
            Annotation[] annotations = attribute.getAnnotations();
            for (Annotation annotation : annotations) {
                set.add(annotation.getTypeName());
            }
        }

        // 删除缓存
        ctClass.detach();

        return set;
    }

    /**
     * 获取父类
     */
    public static Set<String> getSuperClass(String classPath) throws NotFoundException {
        Set<String> set = Sets.newHashSet();
        CtClass ctClass = getCtClass(classPath);
        ClassFile classFile = ctClass.getClassFile();

        String superClass = classFile.getSuperclass();
        if (!"".equals(superClass) && superClass != null && !set.contains(superClass)) {
            set.add(superClass);
        }

        // 删除缓存
        ctClass.detach();

        return set;
    }

    /**
     * 获取接口
     */
    public static Set<String> getInterface(String classPath) throws NotFoundException {
        Set<String> set = Sets.newHashSet();
        CtClass ctClass = getCtClass(classPath);
        ClassFile classFile = ctClass.getClassFile();

        String[] interfaces = classFile.getInterfaces();
        if (interfaces != null) {
            for (String face : interfaces) {
                String className = getClassName(face);
                set.add(className);
            }
        }

        // 删除缓存
        ctClass.detach();

        return set;
    }

    /**
     * 获取字段类型
     */
    public static Set<String> getFieldsType(String classPath) throws NotFoundException {
        Set<String> set = Sets.newHashSet();

        CtClass ctClass = getCtClass(classPath);
        ClassFile classFile = ctClass.getClassFile();

        List<FieldInfo> fieldInfoList = classFile.getFields();
        if (fieldInfoList != null) {
            for (FieldInfo fieldInfo : fieldInfoList) {
                String descriptor = fieldInfo.getDescriptor();
                if (descriptor.startsWith("L") && descriptor.endsWith(";")) {
                    String className = descriptor.substring(1, descriptor.length() - 1);
                    className = getClassName(className);
                    set.add(className);
                }

                if (descriptor.startsWith("[L") && descriptor.endsWith(";")) {
                    String className = descriptor.substring(2, descriptor.length() - 1);
                    className = getClassName(className);
                    set.add(className);
                }
            }
        }

        // 删除缓存
        ctClass.detach();

        return set;
    }

    /**
     * 遍历代码内使用的类，包含方法实现里使用的类，不包含方法签名里的类
     */
    public static Set<String> getClassRelyClass(String classPath) throws NotFoundException {
        Set<String> set = Sets.newHashSet();
        CtClass ctClass = getCtClass(classPath);
        ClassFile classFile = ctClass.getClassFile();

        Set<String> classNames = classFile.getConstPool()
                .getClassNames();
        for (String className : classNames) {
            if (className.startsWith("[L")) {
                className = className.substring(2, className.length() - 1);
            } else if (className.startsWith("[")) {
                continue;
            }
            className = getClassName(className);
            set.add(className);
        }

        // 删除缓存
        ctClass.detach();

        return set;
    }

    /**
     * 获取方法声明的参数和返回值包含的所有类
     */
    public static Set<String> getMethodSignatureClass(String classPath) throws NotFoundException {
        Set<String> set = Sets.newHashSet();
        CtClass ctClass = getCtClass(classPath);
        ClassFile classFile = ctClass.getClassFile();

        CtMethod[] declaredMethods = ctClass.getDeclaredMethods();
        for (CtMethod declaredMethod : declaredMethods) {
            MethodInfo methodInfo = classFile.getMethod(declaredMethod.getName());
            String descriptor = methodInfo.getDescriptor();
            extractClassNames(descriptor, set);
        }
        // 删除缓存
        ctClass.detach();
        return set;
    }

    public static Set<String> getMethodSignatureClass(String classPath, String methodName) throws NotFoundException {
        Set<String> set = Sets.newHashSet();
        CtClass ctClass = getCtClass(classPath);
        ClassFile classFile = ctClass.getClassFile();

        MethodInfo methodInfo = classFile.getMethod(methodName);
        String descriptor = methodInfo.getDescriptor();
        extractClassNames(descriptor, set);

        // 删除缓存
        ctClass.detach();

        return set;
    }

    /**
     * 获取默认的ClassPool
     */
    public static CtClass getCtClass(String classPath) throws NotFoundException {
        try {
            ClassPool classPool = new ClassPool(true);
            // 为防止项目被打成jar包，请使用该语句
            classPool.insertClassPath(new ClassClassPath(Thread.currentThread()
                    .getContextClassLoader()
                    .loadClass(classPath)));
            return classPool.getCtClass(classPath);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 过滤类名
     */
    private static void extractClassNames(String descriptor, Set<String> set) {
        String reg = "(L.+?;)";
        Pattern pattern = Pattern.compile(reg);
        Matcher matcher = pattern.matcher(descriptor);
        while (matcher.find()) {
            String className = matcher.group();
            className = className.substring(1, className.length() - 1);
            className = getClassName(className);
            set.add(className);
        }
    }

    /**
     * 路径替换
     */
    private static String getClassName(String className) {
        return className.replaceAll("/", ".");
    }
}