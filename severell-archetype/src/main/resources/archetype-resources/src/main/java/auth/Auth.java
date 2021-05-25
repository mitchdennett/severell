package ${package}.auth;

import com.severell.core.crypto.PasswordUtils;
import com.severell.core.http.NeedsRequest;
import ${package}.models.User;
import ${package}.models.query.QUser;


import java.util.List;

public class Auth extends NeedsRequest {

    public boolean login(String username, String password) {
        User user = DB.find(User.class).where().eq("email", username).findOne();
        boolean auth = false;
        if(user != null) {
            auth = PasswordUtils.checkPassword(password, user.getPassword());
            if(auth) {
                request.session().put("userid", user);
            }
        }

        return auth;
    }

}