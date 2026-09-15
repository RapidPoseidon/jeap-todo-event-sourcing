-- Read models derived from the event stream; both can be dropped and rebuilt by replaying events.

CREATE TABLE IF NOT EXISTS RM_TASK (
    ID                 UUID                     PRIMARY KEY,
    VERSION            INTEGER                  NOT NULL,
    STATUS             TEXT                     NOT NULL,
    TITLE              TEXT                     NOT NULL,
    DESCRIPTION        TEXT,
    PRIORITY           TEXT                     NOT NULL,
    DUE_DATE           DATE,
    OWNER              TEXT                     NOT NULL,
    ASSIGNEE           TEXT,
    CREATED_BY         TEXT,
    CREATED_DATE       TIMESTAMP WITH TIME ZONE NOT NULL,
    LAST_MODIFIED_DATE TIMESTAMP WITH TIME ZONE NOT NULL,
    COMPLETED_DATE     TIMESTAMP WITH TIME ZONE,
    COMPLETION_COUNT   INTEGER                  NOT NULL DEFAULT 0
);

CREATE INDEX IF NOT EXISTS IDX_RM_TASK_OWNER_STATUS ON RM_TASK (OWNER, STATUS);

CREATE TABLE IF NOT EXISTS RM_TASK_STATISTICS (
    OWNER           TEXT                     PRIMARY KEY,
    CREATED_COUNT   BIGINT                   NOT NULL DEFAULT 0,
    COMPLETED_COUNT BIGINT                   NOT NULL DEFAULT 0,
    REOPENED_COUNT  BIGINT                   NOT NULL DEFAULT 0,
    DELETED_COUNT   BIGINT                   NOT NULL DEFAULT 0,
    LAST_UPDATED    TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now()
);
