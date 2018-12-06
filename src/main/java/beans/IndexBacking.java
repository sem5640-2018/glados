package beans;

import beans.helpers.LoginCheck;
import configuration.EnvironmentVariables;

import javax.ejb.Stateless;
import javax.faces.context.FacesContext;
import javax.inject.Named;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Named
@Stateless
public class IndexBacking extends LoginCheck {

    private String loginPageUrl = EnvironmentVariables.getAppBaseUrl() + "/login.xhtml";

    public IndexBacking(){
        super();
    }

    public void onLoad(){
        boolean isLoggedIn = checkUserLogin();
        if (isLoggedIn){
            HttpServletResponse response = (HttpServletResponse) FacesContext.getCurrentInstance().getExternalContext().getResponse();
            try {
                response.sendRedirect(EnvironmentVariables.getAppBaseUrl() + "/userDataLookup.xhtml");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getLoginPageUrl() {
        return loginPageUrl;
    }
}
