package com.CapstoneProject.capstone.constant;

public class UrlConstant {

    public static final String API = "/api";
    public static class USER {
        public static final String USER = API + "/user";
        public static final String REGISTER = "/register";
        public static final String LOGIN = "/auth";
        public static final String GET = "";
        public static final String GET_ALL = "/get-all";
        public static final String GET_BY_ID = "/{id}";
        public static final String DELETE = "/{id}";
        public static final String CHANGE_PASSWORD = "/change-password";
        public static final String UPLOAD_AVATAR = "/upload-avatar";
        public static final String UPDATE_PROFILE = "/update-profile";
        public static final String LOGIN_GOOGLE = "/google-auth/login";
        public static final String LOGIN_FACEBOOK = "/facebook-auth/login";
        public static final String LOGIN_GITHUB = "/github-auth/login";
        public static final String VERIFY_EMAIL = "/verify-email";
        public static final String RESEND_EMAIL = "/resend-email";
        public static final String FORGOT_PASSWORD = "/forgot-password";
        public static final String CHANGE_FORGOT_PASSWORD = "/change-forgot-password";
    }

    public static class PROJECT {
        public static final String PROJECT = API + "/project";
        public static final String CREATE = "/create";
        public static final String GET_PROJECT = "/{id}";
        public static final String GET_PROJECTS = "";
        public static final String GET_PROJECTS_BY_USER = "/user";
        public static final String DELETE_PROJECT = "/{id}";
        public static final String UPDATE_PROJECT = "/{id}";
        public static final String INVITE_PROJECT = "/{id}/invite";
        public static final String DELETE_MEMBER = "/{id}/delete/member";
        public static final String CLOSE_PROJECT = "/{id}/close";
        public static final String JOIN_PROJECT = "/join";
        public static final String PROCESS_PROJECT = "/{id}/process";
    }

    public static class TOPIC {
        public static final String TOPIC = API + "/topic";
        public static final String CREATE = "/create";
        public static final String GET_TOPICS = "";
        public static final String GET_TOPIC = "/{id}";
        public static final String UPDATE_TOPIC = "/{id}";
        public static final String DELETE_TOPIC = "/{id}";
    }

    public static class TASK {
        public static final String TASK = API + "/task";
        public static final String CREATE = "/create";
        public static final String GET_TASKS = "";
        public static final String GET_TASK = "/{id}";
        public static final String UPDATE_TASKS = "/{id}";
        public static final String DELETE_TASKS = "/{id}";
        public static final String CREATE_ISSUE_BY_TASK = "/{id}/issue";
        public static final String UPDATE_TASK = "/{id}/update";
        public static final String GET_ALL_TASKS_BY_PROJECT = "/task/{projectId}/project";
    }

    public static class ISSUE {
        public static final String ISSUE = API + "/issue";
        public static final String CREATE = "/create";
        public static final String GET_ISSUES = "";
        public static final String GET_ISSUE = "/{id}";
        public static final String GET_ISSUE_BY_TASK = "/{id}/task";
        public static final String UPDATE_ISSUE = "/{id}";
        public static final String DELETE_ISSUE = "/{id}";
        public static final String UPDATE_ISSUES = "/{id}/update";
        public static final String GET_ALL_ISSUES_BY_PROJECT = "/issue/{projectId}/project";
    }

    public static class QUESTION {
        public static final String QUESTION = API + "/question";
        public static final String CREATE = "/create";
        public static final String GET_QUESTIONS = "";
        public static final String GET_QUESTION = "/{id}";
        public static final String UPDATE_QUESTION = "/{id}";
    }

    public static class MEMBER {
        public static final String MEMBER = API + "/member";
        public static final String GET_MEMBERS = "/{projectId}";
        public static final String GET_MEMBERS_PENDING = "/{projectId}/pending";
        public static final String UPDATE_STATUS_MEMBER = "/{projectId}/status";
    }

    public static class FILE {
        public static final String FILE = API + "/file";
        public static final String ADD_FILE_IN_TASK = "/{id}/task";
        public static final String ADD_FILE_IN_ISSUE = "/{id}/issue";
        public static final String DELETE_FILE = "/delete/{id}/file";
        public static final String ADD_FILE_TO_PROJECT = "/{id}/project";
        public static final String GET_ALL_FILES_IN_PROJECT = "/{id}/project";
    }

    public static class ADMIN {
        public static final String ADMIN = API + "/admin";
        public static final String GET_PROJECTS = "/project";
        public static final String DASHBOARD = "/dashboard";
    }

    public static class NEWS {
        public static final String NEWS = API + "/news";
        public static final String CREATE = "/create";
        public static final String GET_ALL_NEWS = "";
        public static final String GET_NEWS = "/{id}";
        public static final String UPDATE_NEWS = "/{id}";
        public static final String DELETE_NEWS = "/{id}";
        public static final String UPLOAD_IMAGE = "/image";
    }

    public static class FEEDBACK {
        public static final String FEEDBACK = API + "/feedback";
        public static final String CREATE = "/create";
        public static final String GET_ALL_FEEDBACK = "";
        public static final String GET_FEEDBACK = "/{id}";
        public static final String DELETE_FEEDBACK = "/{id}";
    }

    public static class PROJECT_ACTIVITY_LOG {
        public static final String PROJECT_ACTIVITY = API + "/activity";
        public static final String GET_ALL = "/{projectId}";
    }

    public static class NOTIFICATION {
        public static final String NOTIFICATION = API + "/notification";
        public static final String GET_ALL = "";
    }

}
