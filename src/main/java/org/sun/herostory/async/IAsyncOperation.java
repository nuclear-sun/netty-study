package org.sun.herostory.async;

/**
 * 框架保证，bind 在调用者线程， doAsync 在异步线程池， finish 在主业务线程
 */
public interface IAsyncOperation {

    /**
     * 某些操作可能要绑定到特定线程上执行，保证执行的顺序性，
     * 如登录时，如果用户名不存在就在数据库中创建新的，如果用户多次点击登录，
     * 如果在不同线程中执行查找，插入操作，导致数据库数据不正确。
     * 如果能保证始终在一个线程中执行可以保证前一次操作完整后才做下一次
     */
    default int bind() {
        return 0;
    }

    /**
     * 执行异步操作
     */
    void doAsync();

    /**
     * 异步操作完成后的清理操作
     */
    default void finish() {};
}
