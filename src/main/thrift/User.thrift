namespace java com.qinzhaokun.thrift // 定义命名空间

// include "test.thrift" // 包含外部的接口文件

/* 定义一个Person类 */
struct User
{
    1: i32 id, // Id
    2: string username, // 名称
    3: optional string nickname, // 昵称
    4: optional i32 gender, // 性别，1：男，2：女，3：其他
    5: optional string email, // 邮箱
}


/* 服务接口 */
service IUserService
{
    /* 添加一个用户 */
    void add(1:User user),

    /* 根据Id删除一个用户 */
    void deleteById(1:i32 id),

    /* 根据Id更新一个用户 */
    void updateById(1:User user),

    /* 根据Id查询一个用户 */
    User findById(1:i32 id),

    /* 查询全部用户 */
    list<User> findAll(),
}