CREATE TABLE IF NOT EXISTS sys_user (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    username    VARCHAR(64)  NOT NULL UNIQUE,
    password    VARCHAR(128) NOT NULL,
    role        VARCHAR(32)  NOT NULL,
    real_name   VARCHAR(64),
    org_name    VARCHAR(128),
    phone       VARCHAR(32),
    enabled     TINYINT      NOT NULL DEFAULT 1,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS trial_project (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_no   VARCHAR(32)  NOT NULL UNIQUE,
    title        VARCHAR(256) NOT NULL,
    enterprise_id BIGINT,
    stage        VARCHAR(32)  NOT NULL DEFAULT 'DEMAND',
    status       VARCHAR(32)  NOT NULL DEFAULT 'DRAFT',
    current_node VARCHAR(64),
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS notification (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id     BIGINT       NOT NULL,
    project_id  BIGINT,
    type        VARCHAR(32),
    title       VARCHAR(256),
    content     VARCHAR(512),
    read_flag   TINYINT      NOT NULL DEFAULT 0,
    created_at  TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS demand (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id     BIGINT       NOT NULL,
    content        CLOB,
    pilot_type     VARCHAR(64),
    expected_days  INT,
    contact_name   VARCHAR(64),
    contact_phone  VARCHAR(32),
    reject_reason  VARCHAR(512),
    accept_opinion VARCHAR(512),
    accept_result  VARCHAR(32),
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS demand_material (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    demand_id      BIGINT       NOT NULL,
    file_url       VARCHAR(512) NOT NULL,
    file_name      VARCHAR(256),
    material_type  VARCHAR(64),
    version        INT          NOT NULL DEFAULT 1,
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS workflow_log (
    id           BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id   BIGINT       NOT NULL,
    from_status  VARCHAR(32),
    to_status    VARCHAR(32),
    operator_id  BIGINT,
    remark       VARCHAR(512),
    created_at   TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS evaluation (
    id                  BIGINT AUTO_INCREMENT PRIMARY KEY,
    project_id          BIGINT       NOT NULL,
    precheck_remark     VARCHAR(512),
    condition_result    VARCHAR(32),
    condition_remark    VARCHAR(512),
    rectify_notice      VARCHAR(512),
    resource_remark     VARCHAR(512),
    resource_requirement VARCHAR(512),
    feasibility_result  VARCHAR(32),
    feasibility_remark  VARCHAR(512),
    conclusion          TEXT,
    conclusion_opinion  VARCHAR(512),
    feedback_content    VARCHAR(512),
    created_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS evaluation_material (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    evaluation_id  BIGINT       NOT NULL,
    file_url       VARCHAR(512) NOT NULL,
    file_name      VARCHAR(256),
    material_type  VARCHAR(64),
    version        INT          NOT NULL DEFAULT 1,
    created_at     TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
);
