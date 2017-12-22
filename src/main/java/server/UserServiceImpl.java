package server;

import com.qinzhaokun.thrift.IUserService;
import com.qinzhaokun.thrift.User;
import org.apache.thrift.TException;

import java.util.List;

public class UserServiceImpl implements IUserService.Iface{

    public void add(User user) throws TException {

    }

    public List<User> findAll() throws TException {
        return null;
    }

    public User findById(int id) throws TException {
        User user = new User();
        user.setId(id);
        user.setEmail(id+"@antfin.com");
        return user;
    }

    public void deleteById(int id) throws TException {

    }

    public void updateById(User user) throws TException {

    }
}
