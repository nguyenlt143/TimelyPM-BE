package com.CapstoneProject.capstone.constant;

public class UrlConstant {

    public static final String API = "/api";
    public static class USER {
        public static final String USER = API + "/user";
        public static final String REGISTER = "/register";
        public static final String LOGIN = "/auth";
<<<<<<< HEAD
=======
        public static final String GET = "";
>>>>>>> a17adb759a5f60a26e573478b71627fc5b7fb7d8
    }

    public static class PROJECT {
        public static final String PROJECT = API + "/project";
        public static final String CREATE = "/create";
        public static final String GET_PROJECT = "/{id}";
        public static final String GET_PROJECTS = "";
        public static final String DELETE_PROJECT = "/{id}";
        public static final String UPDATE_PROJECT = "/{id}";
        public static final String INVITE_PROJECT = "/{id}/invite";
        public static final String DELETE_MEMBER = "/{id}/delete/member";
    }

<<<<<<< HEAD
=======
    public static class TOPIC {
        public static final String TOPIC = API + "/topic";
        public static final String CREATE = "/create";
        public static final String GET_TOPICS = "";
    }

>>>>>>> a17adb759a5f60a26e573478b71627fc5b7fb7d8
}
