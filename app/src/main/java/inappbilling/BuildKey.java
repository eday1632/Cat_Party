package inappbilling;

/* builds the unique key for the app */
public class BuildKey{

    public BuildKey(){

    }
    /*key built by concatenating several strings because it is a poor security practice to
    * have the key hard coded as one string */
    public String getKey(){
        String first = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjZDmpMgGgPGaxZ1SZU";
        String second = "wsHq+pB9u95C9SnvcWzoi4Z4wLb8EvVgNLl4/+SNguYNMvpQqOEK/T0oQtOWNpI9413Gq4RPcf5";
        String third = "j4W57tfIDA78s04VA4xTPZLiybQJlHbUHOSKNZLHN7k5uabWz4G+KLUMC6kciFSsZb2e9IgPTWnkA2YueVWf";
        String fourth = "hDH+CS6BVL+YQQJq9OTeX+j1USSmgHZHd6IO0rHzxUM8Gv2UzgGKLmpitKeUlemtkigGRRs6ss91sSJJ8S1cf";
        String fifth = "ISxkKrSga45A54nZc10LCO9RyLm6zyLuXa2fJAeUprXx6QvXjUX6oGutDJuaEiiCKJFmS3FBo1aP/sPwIDAQAB";
        String last;



        last = first.concat(second);
        last = last.concat(third);
        last = last.concat(fourth).concat(fifth);

        return last;
    }

}
