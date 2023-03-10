package dml.test.repository;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public abstract class TestRepository<E, ID> {

    protected Map<Object, E> data = new HashMap<>();

    public E find(ID id) {
        return data.get(id);
    }

    public E take(ID id) {
        return data.get(id);
    }

    public void put(E entity) {
        data.put(getId(entity), entity);
    }

    public E putIfAbsent(E entity) {
        return data.putIfAbsent(getId(entity), entity);
    }

    public E takeOrPutIfAbsent(ID id, E newEntity) {
        E entity = take(id);
        if (entity != null) {
            return entity;
        }
        E exists = putIfAbsent(newEntity);
        if (exists != null) {
            return exists;
        }
        return newEntity;
    }

    public E remove(ID id) {
        return data.remove(id);
    }

    private ID getId(E entity) {
        Field idField;
        try {
            idField = entity.getClass().getDeclaredField("id");
        } catch (NoSuchFieldException e) {
            throw new RuntimeException("getDeclaredField 'id' error", e);
        }
        idField.setAccessible(true);
        try {
            return (ID) idField.get(entity);
        } catch (IllegalAccessException e) {
            throw new RuntimeException("get value of idField error", e);
        }
    }

    public static <I> I instance(Class<I> itfType) {
        TestRepository testRepositoryInstance = new TestRepository() {
        };
        I instance = (I) Proxy.newProxyInstance(testRepositoryInstance.getClass().getClassLoader(), new Class[]{itfType},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if ("find".equals(method.getName())) {
                            return testRepositoryInstance.find(args[0]);
                        } else if ("take".equals(method.getName())) {
                            return testRepositoryInstance.take(args[0]);
                        } else if ("put".equals(method.getName())) {
                            testRepositoryInstance.put(args[0]);
                            return null;
                        } else if ("putIfAbsent".equals(method.getName())) {
                            testRepositoryInstance.putIfAbsent(args[0]);
                            return null;
                        } else if ("takeOrPutIfAbsent".equals(method.getName())) {
                            return testRepositoryInstance.takeOrPutIfAbsent(args[0], args[1]);
                        } else if ("remove".equals(method.getName())) {
                            testRepositoryInstance.remove(args[0]);
                            return null;
                        } else {
                            throw new UnsupportedOperationException(method.getName());
                        }
                    }
                });
        return instance;
    }

}
