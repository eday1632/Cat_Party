package inappbilling;

/* builds the unique key for the app */
public class BuildKey{

    public BuildKey(){

    }
    /*key built by concatenating several strings because it is a poor security practice to
    * have the key hard coded as one string */
    public String getKey(){
        String first = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAoqc/Zr0EAstvPEQ3RCfPNEz5ho1+FncedWvz";
        String second = "/9VKy4mQkpuSdV9SHgFAYe8X3PkP7ngbpbQxdLAsTfuoUdRqwd9RlvA2BhsAxmgrfCgKSfnp9v";
        String third = "+swuWa0suDPEY0hMS2SOvdgTZRaXP8RqJz0c7qQPbT5i+lheYAu1HJ6FAQvCWOeA6eV6C0qkO0CKSscBXZiKYhpKem3mumiac";
        String fourth = "+yba0Bbea1wz49yGIhCVyGAWBySbAYv1a3TTE2VoNgJArC7hZap9NF1IDezIaLBgblC2LQizooUlmZxTcQPp6YRT8DGmRl";
        String fifth = "+V6SFFalAxAYM2Uc3Fa52LjMUqNBxKDzu7vG1grBQIDAQAB";
        String last;



        last = first.concat(second);
        last = last.concat(third);
        last = last.concat(fourth).concat(fifth);

        return last;
    }

}
