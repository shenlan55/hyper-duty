--
-- PostgreSQL database dump
--

\restrict Ktio33LCUIbNfkVFWYN0IA5hWYcczvHJovEbN5VPeGIMhr6buT7budcp8qMztfg

-- Dumped from database version 18.2 (Debian 18.2-1.pgdg13+1)
-- Dumped by pg_dump version 18.2

-- Started on 2026-02-24 20:59:48

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- TOC entry 2 (class 3079 OID 16389)
-- Name: uuid-ossp; Type: EXTENSION; Schema: -; Owner: -
--

CREATE EXTENSION IF NOT EXISTS "uuid-ossp" WITH SCHEMA public;


--
-- TOC entry 4079 (class 0 OID 0)
-- Dependencies: 2
-- Name: EXTENSION "uuid-ossp"; Type: COMMENT; Schema: -; Owner: 
--

COMMENT ON EXTENSION "uuid-ossp" IS 'generate universally unique identifiers (UUIDs)';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 265 (class 1259 OID 34595)
-- Name: approval_record; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.approval_record (
    id bigint NOT NULL,
    request_id bigint NOT NULL,
    request_type character varying(20) NOT NULL,
    approver_id bigint NOT NULL,
    approval_level integer NOT NULL,
    approval_status character varying(20) NOT NULL,
    approval_opinion character varying(500) DEFAULT NULL::character varying,
    approval_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.approval_record OWNER TO postgres;

--
-- TOC entry 4080 (class 0 OID 0)
-- Dependencies: 265
-- Name: TABLE approval_record; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.approval_record IS '审批记录表';


--
-- TOC entry 4081 (class 0 OID 0)
-- Dependencies: 265
-- Name: COLUMN approval_record.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.approval_record.id IS '审批记录ID';


--
-- TOC entry 4082 (class 0 OID 0)
-- Dependencies: 265
-- Name: COLUMN approval_record.request_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.approval_record.request_id IS '申请ID';


--
-- TOC entry 4083 (class 0 OID 0)
-- Dependencies: 265
-- Name: COLUMN approval_record.request_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.approval_record.request_type IS '申请类型:leave-请假,swap-调班';


--
-- TOC entry 4084 (class 0 OID 0)
-- Dependencies: 265
-- Name: COLUMN approval_record.approver_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.approval_record.approver_id IS '审批人ID';


--
-- TOC entry 4085 (class 0 OID 0)
-- Dependencies: 265
-- Name: COLUMN approval_record.approval_level; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.approval_record.approval_level IS '审批层级';


--
-- TOC entry 4086 (class 0 OID 0)
-- Dependencies: 265
-- Name: COLUMN approval_record.approval_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.approval_record.approval_status IS '审批状态:approved-通过,rejected-拒绝';


--
-- TOC entry 4087 (class 0 OID 0)
-- Dependencies: 265
-- Name: COLUMN approval_record.approval_opinion; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.approval_record.approval_opinion IS '审批意见';


--
-- TOC entry 4088 (class 0 OID 0)
-- Dependencies: 265
-- Name: COLUMN approval_record.approval_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.approval_record.approval_time IS '审批时间';


--
-- TOC entry 4089 (class 0 OID 0)
-- Dependencies: 265
-- Name: COLUMN approval_record.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.approval_record.create_time IS '创建时间';


--
-- TOC entry 264 (class 1259 OID 34594)
-- Name: approval_record_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.approval_record_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.approval_record_id_seq OWNER TO postgres;

--
-- TOC entry 4090 (class 0 OID 0)
-- Dependencies: 264
-- Name: approval_record_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.approval_record_id_seq OWNED BY public.approval_record.id;


--
-- TOC entry 255 (class 1259 OID 34391)
-- Name: duty_assignment; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.duty_assignment (
    id bigint NOT NULL,
    schedule_id bigint NOT NULL,
    duty_date date NOT NULL,
    duty_shift integer DEFAULT 1,
    employee_id bigint NOT NULL,
    status smallint DEFAULT '1'::smallint,
    remark character varying(200) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    shift_config_id bigint,
    version_id bigint,
    is_overtime smallint DEFAULT '0'::smallint
);


ALTER TABLE public.duty_assignment OWNER TO postgres;

--
-- TOC entry 4091 (class 0 OID 0)
-- Dependencies: 255
-- Name: TABLE duty_assignment; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.duty_assignment IS '值班安排表';


--
-- TOC entry 4092 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN duty_assignment.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_assignment.id IS '值班安排ID';


--
-- TOC entry 4093 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN duty_assignment.schedule_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_assignment.schedule_id IS '关联值班表ID';


--
-- TOC entry 4094 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN duty_assignment.duty_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_assignment.duty_date IS '值班日期';


--
-- TOC entry 4095 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN duty_assignment.duty_shift; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_assignment.duty_shift IS '值班班次：1白班，2晚班，3夜班';


--
-- TOC entry 4096 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN duty_assignment.employee_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_assignment.employee_id IS '值班人员ID';


--
-- TOC entry 4097 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN duty_assignment.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_assignment.status IS '状态：0取消，1正常';


--
-- TOC entry 4098 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN duty_assignment.remark; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_assignment.remark IS '备注';


--
-- TOC entry 4099 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN duty_assignment.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_assignment.create_time IS '创建时间';


--
-- TOC entry 4100 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN duty_assignment.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_assignment.update_time IS '更新时间';


--
-- TOC entry 4101 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN duty_assignment.shift_config_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_assignment.shift_config_id IS '班次配置ID';


--
-- TOC entry 4102 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN duty_assignment.version_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_assignment.version_id IS '排班版本ID';


--
-- TOC entry 4103 (class 0 OID 0)
-- Dependencies: 255
-- Name: COLUMN duty_assignment.is_overtime; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_assignment.is_overtime IS '是否加班:0-否,1-是';


--
-- TOC entry 254 (class 1259 OID 34390)
-- Name: duty_assignment_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.duty_assignment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.duty_assignment_id_seq OWNER TO postgres;

--
-- TOC entry 4104 (class 0 OID 0)
-- Dependencies: 254
-- Name: duty_assignment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.duty_assignment_id_seq OWNED BY public.duty_assignment.id;


--
-- TOC entry 235 (class 1259 OID 34141)
-- Name: duty_holiday; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.duty_holiday (
    id bigint NOT NULL,
    holiday_name character varying(50) NOT NULL,
    holiday_date date NOT NULL,
    is_workday smallint DEFAULT '0'::smallint,
    holiday_type smallint DEFAULT '1'::smallint,
    remark character varying(200) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.duty_holiday OWNER TO postgres;

--
-- TOC entry 4105 (class 0 OID 0)
-- Dependencies: 235
-- Name: TABLE duty_holiday; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.duty_holiday IS '节假日表';


--
-- TOC entry 4106 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN duty_holiday.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_holiday.id IS '节假日ID';


--
-- TOC entry 4107 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN duty_holiday.holiday_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_holiday.holiday_name IS '节假日名称';


--
-- TOC entry 4108 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN duty_holiday.holiday_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_holiday.holiday_date IS '节假日日期';


--
-- TOC entry 4109 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN duty_holiday.is_workday; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_holiday.is_workday IS '是否调休上班:0-否,1-是';


--
-- TOC entry 4110 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN duty_holiday.holiday_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_holiday.holiday_type IS '节假日类型:1-法定假日,2-调休,3-公司假日';


--
-- TOC entry 4111 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN duty_holiday.remark; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_holiday.remark IS '备注';


--
-- TOC entry 4112 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN duty_holiday.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_holiday.create_time IS '创建时间';


--
-- TOC entry 4113 (class 0 OID 0)
-- Dependencies: 235
-- Name: COLUMN duty_holiday.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_holiday.update_time IS '更新时间';


--
-- TOC entry 234 (class 1259 OID 34140)
-- Name: duty_holiday_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.duty_holiday_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.duty_holiday_id_seq OWNER TO postgres;

--
-- TOC entry 4114 (class 0 OID 0)
-- Dependencies: 234
-- Name: duty_holiday_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.duty_holiday_id_seq OWNED BY public.duty_holiday.id;


--
-- TOC entry 267 (class 1259 OID 34625)
-- Name: duty_record; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.duty_record (
    id bigint NOT NULL,
    assignment_id bigint NOT NULL,
    employee_id bigint NOT NULL,
    check_in_time timestamp without time zone,
    check_out_time timestamp without time zone,
    duty_status smallint DEFAULT '1'::smallint,
    check_in_remark character varying(200) DEFAULT NULL::character varying,
    check_out_remark character varying(200) DEFAULT NULL::character varying,
    overtime_hours numeric(5,2) DEFAULT 0.00,
    approval_status character varying(20) DEFAULT 'pending'::character varying,
    manager_remark character varying(200) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    substitute_employee_id bigint,
    substitute_type smallint DEFAULT '1'::smallint
);


ALTER TABLE public.duty_record OWNER TO postgres;

--
-- TOC entry 4115 (class 0 OID 0)
-- Dependencies: 267
-- Name: TABLE duty_record; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.duty_record IS '值班记录表';


--
-- TOC entry 4116 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.id IS '值班记录ID';


--
-- TOC entry 4117 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.assignment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.assignment_id IS '关联值班安排ID';


--
-- TOC entry 4118 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.employee_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.employee_id IS '值班人员ID';


--
-- TOC entry 4119 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.check_in_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.check_in_time IS '签到时间';


--
-- TOC entry 4120 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.check_out_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.check_out_time IS '签退时间';


--
-- TOC entry 4121 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.duty_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.duty_status IS '值班状态：1正常，2迟到，3早退，4缺勤，5请假';


--
-- TOC entry 4122 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.check_in_remark; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.check_in_remark IS '签到备注';


--
-- TOC entry 4123 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.check_out_remark; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.check_out_remark IS '签退备注';


--
-- TOC entry 4124 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.overtime_hours; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.overtime_hours IS '加班时长';


--
-- TOC entry 4125 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.approval_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.approval_status IS '审批状态：pending待审批，approved已审批，rejected已拒绝';


--
-- TOC entry 4126 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.manager_remark; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.manager_remark IS '经理备注';


--
-- TOC entry 4127 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.create_time IS '创建时间';


--
-- TOC entry 4128 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.update_time IS '更新时间';


--
-- TOC entry 4129 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.substitute_employee_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.substitute_employee_id IS '替补人员ID';


--
-- TOC entry 4130 (class 0 OID 0)
-- Dependencies: 267
-- Name: COLUMN duty_record.substitute_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_record.substitute_type IS '替补类型:1-自动匹配,2-手动选择';


--
-- TOC entry 266 (class 1259 OID 34624)
-- Name: duty_record_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.duty_record_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.duty_record_id_seq OWNER TO postgres;

--
-- TOC entry 4131 (class 0 OID 0)
-- Dependencies: 266
-- Name: duty_record_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.duty_record_id_seq OWNED BY public.duty_record.id;


--
-- TOC entry 243 (class 1259 OID 34253)
-- Name: duty_schedule; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.duty_schedule (
    id bigint NOT NULL,
    schedule_name character varying(100) NOT NULL,
    description character varying(200) DEFAULT NULL::character varying,
    start_date date NOT NULL,
    end_date date NOT NULL,
    status smallint DEFAULT '1'::smallint,
    create_by bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    schedule_mode_id bigint,
    sort_order integer DEFAULT 0
);


ALTER TABLE public.duty_schedule OWNER TO postgres;

--
-- TOC entry 4132 (class 0 OID 0)
-- Dependencies: 243
-- Name: TABLE duty_schedule; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.duty_schedule IS '值班表';


--
-- TOC entry 4133 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN duty_schedule.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule.id IS '值班表ID';


--
-- TOC entry 4134 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN duty_schedule.schedule_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule.schedule_name IS '值班表名称';


--
-- TOC entry 4135 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN duty_schedule.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule.description IS '描述';


--
-- TOC entry 4136 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN duty_schedule.start_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule.start_date IS '开始日期';


--
-- TOC entry 4137 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN duty_schedule.end_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule.end_date IS '结束日期';


--
-- TOC entry 4138 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN duty_schedule.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule.status IS '状态：0禁用，1启用';


--
-- TOC entry 4139 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN duty_schedule.create_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule.create_by IS '创建人ID';


--
-- TOC entry 4140 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN duty_schedule.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule.create_time IS '创建时间';


--
-- TOC entry 4141 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN duty_schedule.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule.update_time IS '更新时间';


--
-- TOC entry 4142 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN duty_schedule.schedule_mode_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule.schedule_mode_id IS '排班方式ID';


--
-- TOC entry 4143 (class 0 OID 0)
-- Dependencies: 243
-- Name: COLUMN duty_schedule.sort_order; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule.sort_order IS '排序';


--
-- TOC entry 249 (class 1259 OID 34320)
-- Name: duty_schedule_employee; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.duty_schedule_employee (
    id bigint NOT NULL,
    schedule_id bigint NOT NULL,
    employee_id bigint NOT NULL,
    sort_order integer DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_leader smallint DEFAULT '0'::smallint
);


ALTER TABLE public.duty_schedule_employee OWNER TO postgres;

--
-- TOC entry 4144 (class 0 OID 0)
-- Dependencies: 249
-- Name: TABLE duty_schedule_employee; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.duty_schedule_employee IS '值班表人员关联表';


--
-- TOC entry 4145 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN duty_schedule_employee.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_employee.id IS 'ID';


--
-- TOC entry 4146 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN duty_schedule_employee.schedule_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_employee.schedule_id IS '值班表ID';


--
-- TOC entry 4147 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN duty_schedule_employee.employee_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_employee.employee_id IS '员工ID';


--
-- TOC entry 4148 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN duty_schedule_employee.sort_order; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_employee.sort_order IS '排序';


--
-- TOC entry 4149 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN duty_schedule_employee.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_employee.create_time IS '创建时间';


--
-- TOC entry 4150 (class 0 OID 0)
-- Dependencies: 249
-- Name: COLUMN duty_schedule_employee.is_leader; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_employee.is_leader IS '是否值班长：0否，1是';


--
-- TOC entry 248 (class 1259 OID 34319)
-- Name: duty_schedule_employee_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.duty_schedule_employee_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.duty_schedule_employee_id_seq OWNER TO postgres;

--
-- TOC entry 4151 (class 0 OID 0)
-- Dependencies: 248
-- Name: duty_schedule_employee_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.duty_schedule_employee_id_seq OWNED BY public.duty_schedule_employee.id;


--
-- TOC entry 242 (class 1259 OID 34252)
-- Name: duty_schedule_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.duty_schedule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.duty_schedule_id_seq OWNER TO postgres;

--
-- TOC entry 4152 (class 0 OID 0)
-- Dependencies: 242
-- Name: duty_schedule_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.duty_schedule_id_seq OWNED BY public.duty_schedule.id;


--
-- TOC entry 229 (class 1259 OID 34068)
-- Name: duty_schedule_mode; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.duty_schedule_mode (
    id bigint NOT NULL,
    mode_name character varying(100) NOT NULL,
    mode_code character varying(50) NOT NULL,
    mode_type smallint NOT NULL,
    algorithm_class character varying(200) DEFAULT NULL::character varying,
    config_json json,
    description character varying(500) DEFAULT NULL::character varying,
    status smallint DEFAULT '1'::smallint,
    sort integer DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.duty_schedule_mode OWNER TO postgres;

--
-- TOC entry 4153 (class 0 OID 0)
-- Dependencies: 229
-- Name: TABLE duty_schedule_mode; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.duty_schedule_mode IS '排班方式表';


--
-- TOC entry 4154 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN duty_schedule_mode.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_mode.id IS '排班方式ID';


--
-- TOC entry 4155 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN duty_schedule_mode.mode_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_mode.mode_name IS '排班方式名称';


--
-- TOC entry 4156 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN duty_schedule_mode.mode_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_mode.mode_code IS '排班方式编码';


--
-- TOC entry 4157 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN duty_schedule_mode.mode_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_mode.mode_type IS '排班方式类型:1-轮班制,2-综合制,3-弹性制,4-自定义';


--
-- TOC entry 4158 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN duty_schedule_mode.algorithm_class; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_mode.algorithm_class IS '算法实现类全路径';


--
-- TOC entry 4159 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN duty_schedule_mode.config_json; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_mode.config_json IS '配置参数JSON';


--
-- TOC entry 4160 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN duty_schedule_mode.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_mode.description IS '排班方式描述';


--
-- TOC entry 4161 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN duty_schedule_mode.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_mode.status IS '状态:0-禁用,1-启用';


--
-- TOC entry 4162 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN duty_schedule_mode.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_mode.sort IS '排序';


--
-- TOC entry 4163 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN duty_schedule_mode.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_mode.create_time IS '创建时间';


--
-- TOC entry 4164 (class 0 OID 0)
-- Dependencies: 229
-- Name: COLUMN duty_schedule_mode.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_mode.update_time IS '更新时间';


--
-- TOC entry 228 (class 1259 OID 34067)
-- Name: duty_schedule_mode_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.duty_schedule_mode_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.duty_schedule_mode_id_seq OWNER TO postgres;

--
-- TOC entry 4165 (class 0 OID 0)
-- Dependencies: 228
-- Name: duty_schedule_mode_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.duty_schedule_mode_id_seq OWNED BY public.duty_schedule_mode.id;


--
-- TOC entry 231 (class 1259 OID 34090)
-- Name: duty_schedule_rule; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.duty_schedule_rule (
    id bigint NOT NULL,
    rule_name character varying(100) NOT NULL,
    rule_code character varying(50) NOT NULL,
    schedule_cycle smallint DEFAULT '1'::smallint NOT NULL,
    max_daily_shifts integer DEFAULT 3,
    max_weekly_hours numeric(6,2) DEFAULT 48.00,
    max_monthly_hours numeric(6,2) DEFAULT 200.00,
    min_rest_days integer DEFAULT 4,
    substitute_priority_rule character varying(200) DEFAULT NULL::character varying,
    conflict_detection_rule character varying(200) DEFAULT NULL::character varying,
    status smallint DEFAULT '1'::smallint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.duty_schedule_rule OWNER TO postgres;

--
-- TOC entry 4166 (class 0 OID 0)
-- Dependencies: 231
-- Name: TABLE duty_schedule_rule; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.duty_schedule_rule IS '排班规则配置表';


--
-- TOC entry 4167 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN duty_schedule_rule.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_rule.id IS '规则ID';


--
-- TOC entry 4168 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN duty_schedule_rule.rule_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_rule.rule_name IS '规则名称';


--
-- TOC entry 4169 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN duty_schedule_rule.rule_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_rule.rule_code IS '规则编码';


--
-- TOC entry 4170 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN duty_schedule_rule.schedule_cycle; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_rule.schedule_cycle IS '排班周期:1-按周,2-按月,3-按季度';


--
-- TOC entry 4171 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN duty_schedule_rule.max_daily_shifts; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_rule.max_daily_shifts IS '每日最大班次数';


--
-- TOC entry 4172 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN duty_schedule_rule.max_weekly_hours; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_rule.max_weekly_hours IS '每周最大工作时长(小时)';


--
-- TOC entry 4173 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN duty_schedule_rule.max_monthly_hours; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_rule.max_monthly_hours IS '每月最大工作时长(小时)';


--
-- TOC entry 4174 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN duty_schedule_rule.min_rest_days; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_rule.min_rest_days IS '每月最少休息天数';


--
-- TOC entry 4175 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN duty_schedule_rule.substitute_priority_rule; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_rule.substitute_priority_rule IS '替补优先级规则';


--
-- TOC entry 4176 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN duty_schedule_rule.conflict_detection_rule; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_rule.conflict_detection_rule IS '冲突检测规则';


--
-- TOC entry 4177 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN duty_schedule_rule.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_rule.status IS '状态:0-禁用,1-启用';


--
-- TOC entry 4178 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN duty_schedule_rule.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_rule.create_time IS '创建时间';


--
-- TOC entry 4179 (class 0 OID 0)
-- Dependencies: 231
-- Name: COLUMN duty_schedule_rule.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_rule.update_time IS '更新时间';


--
-- TOC entry 230 (class 1259 OID 34089)
-- Name: duty_schedule_rule_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.duty_schedule_rule_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.duty_schedule_rule_id_seq OWNER TO postgres;

--
-- TOC entry 4180 (class 0 OID 0)
-- Dependencies: 230
-- Name: duty_schedule_rule_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.duty_schedule_rule_id_seq OWNED BY public.duty_schedule_rule.id;


--
-- TOC entry 251 (class 1259 OID 34346)
-- Name: duty_schedule_shift; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.duty_schedule_shift (
    id bigint NOT NULL,
    schedule_id bigint NOT NULL,
    shift_config_id bigint NOT NULL,
    sort_order integer DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.duty_schedule_shift OWNER TO postgres;

--
-- TOC entry 4181 (class 0 OID 0)
-- Dependencies: 251
-- Name: TABLE duty_schedule_shift; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.duty_schedule_shift IS '值班表班次关联表';


--
-- TOC entry 4182 (class 0 OID 0)
-- Dependencies: 251
-- Name: COLUMN duty_schedule_shift.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_shift.id IS 'ID';


--
-- TOC entry 4183 (class 0 OID 0)
-- Dependencies: 251
-- Name: COLUMN duty_schedule_shift.schedule_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_shift.schedule_id IS '值班表ID';


--
-- TOC entry 4184 (class 0 OID 0)
-- Dependencies: 251
-- Name: COLUMN duty_schedule_shift.shift_config_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_shift.shift_config_id IS '班次配置ID';


--
-- TOC entry 4185 (class 0 OID 0)
-- Dependencies: 251
-- Name: COLUMN duty_schedule_shift.sort_order; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_shift.sort_order IS '排序';


--
-- TOC entry 4186 (class 0 OID 0)
-- Dependencies: 251
-- Name: COLUMN duty_schedule_shift.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_schedule_shift.create_time IS '创建时间';


--
-- TOC entry 250 (class 1259 OID 34345)
-- Name: duty_schedule_shift_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.duty_schedule_shift_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.duty_schedule_shift_id_seq OWNER TO postgres;

--
-- TOC entry 4187 (class 0 OID 0)
-- Dependencies: 250
-- Name: duty_schedule_shift_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.duty_schedule_shift_id_seq OWNED BY public.duty_schedule_shift.id;


--
-- TOC entry 233 (class 1259 OID 34115)
-- Name: duty_shift_config; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.duty_shift_config (
    id bigint NOT NULL,
    shift_name character varying(50) NOT NULL,
    shift_code character varying(20) NOT NULL,
    shift_type smallint DEFAULT '1'::smallint NOT NULL,
    start_time time without time zone NOT NULL,
    end_time time without time zone NOT NULL,
    duration_hours numeric(4,2) NOT NULL,
    break_hours numeric(4,2) DEFAULT 0.00,
    rest_day_rule character varying(100) DEFAULT NULL::character varying,
    is_overtime_shift smallint DEFAULT '0'::smallint,
    status smallint DEFAULT '1'::smallint,
    sort integer DEFAULT 0,
    remark character varying(200) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    is_cross_day smallint DEFAULT '0'::smallint
);


ALTER TABLE public.duty_shift_config OWNER TO postgres;

--
-- TOC entry 4188 (class 0 OID 0)
-- Dependencies: 233
-- Name: TABLE duty_shift_config; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.duty_shift_config IS '班次配置表';


--
-- TOC entry 4189 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.id IS '班次配置ID';


--
-- TOC entry 4190 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.shift_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.shift_name IS '班次名称';


--
-- TOC entry 4191 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.shift_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.shift_code IS '班次编码';


--
-- TOC entry 4192 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.shift_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.shift_type IS '班次类型:0-白班,1-早班,2-中班,3-晚班,4-全天,5-夜班';


--
-- TOC entry 4193 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.start_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.start_time IS '上班时间';


--
-- TOC entry 4194 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.end_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.end_time IS '下班时间';


--
-- TOC entry 4195 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.duration_hours; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.duration_hours IS '时长(小时)';


--
-- TOC entry 4196 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.break_hours; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.break_hours IS '休息时长(小时)';


--
-- TOC entry 4197 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.rest_day_rule; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.rest_day_rule IS '休息日规则';


--
-- TOC entry 4198 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.is_overtime_shift; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.is_overtime_shift IS '是否加班班次:0-否,1-是';


--
-- TOC entry 4199 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.status IS '状态:0-禁用,1-启用';


--
-- TOC entry 4200 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.sort IS '排序';


--
-- TOC entry 4201 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.remark; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.remark IS '备注';


--
-- TOC entry 4202 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.create_time IS '创建时间';


--
-- TOC entry 4203 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.update_time IS '更新时间';


--
-- TOC entry 4204 (class 0 OID 0)
-- Dependencies: 233
-- Name: COLUMN duty_shift_config.is_cross_day; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_config.is_cross_day IS '是否跨天:0-否,1-是';


--
-- TOC entry 232 (class 1259 OID 34114)
-- Name: duty_shift_config_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.duty_shift_config_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.duty_shift_config_id_seq OWNER TO postgres;

--
-- TOC entry 4205 (class 0 OID 0)
-- Dependencies: 232
-- Name: duty_shift_config_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--


ALTER SEQUENCE public.duty_shift_config_id_seq OWNED BY public.duty_shift_config.id;

-- 添加任务干系人字段
ALTER TABLE public.pm_task ADD COLUMN stakeholders TEXT;


--
-- 创建项目参与者关联表
--
CREATE TABLE IF NOT EXISTS public.pm_project_participant (
    id SERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES public.pm_project(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE,
    UNIQUE(project_id, employee_id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_pm_project_participant_project_id ON public.pm_project_participant(project_id);
CREATE INDEX IF NOT EXISTS idx_pm_project_participant_employee_id ON public.pm_project_participant(employee_id);

--
-- 创建项目代理负责人关联表
--
CREATE TABLE IF NOT EXISTS public.pm_project_deputy_owner (
    id SERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES public.pm_project(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE,
    UNIQUE(project_id, employee_id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_pm_project_deputy_owner_project_id ON public.pm_project_deputy_owner(project_id);
CREATE INDEX IF NOT EXISTS idx_pm_project_deputy_owner_employee_id ON public.pm_project_deputy_owner(employee_id);

--
-- 创建任务进展更新表
--
CREATE TABLE IF NOT EXISTS public.pm_task_progress_update (
    id BIGSERIAL PRIMARY KEY,
    task_id BIGINT NOT NULL REFERENCES public.pm_task(id) ON DELETE CASCADE,
    employee_id BIGINT NOT NULL REFERENCES public.sys_employee(id),
    progress INTEGER NOT NULL CHECK (progress >= 0 AND progress <= 100),
    description TEXT,
    attachments JSONB,
    create_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_pm_task_progress_update_task_id ON public.pm_task_progress_update(task_id);
CREATE INDEX IF NOT EXISTS idx_pm_task_progress_update_create_time ON public.pm_task_progress_update(create_time);

--
-- TOC entry 245 (class 1259 OID 34274)
-- Name: duty_shift_mutex; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.duty_shift_mutex (
    id bigint NOT NULL,
    shift_config_id bigint NOT NULL,
    mutex_shift_config_id bigint NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.duty_shift_mutex OWNER TO postgres;

--
-- TOC entry 4206 (class 0 OID 0)
-- Dependencies: 245
-- Name: TABLE duty_shift_mutex; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.duty_shift_mutex IS '班次互斥关系表';


--
-- TOC entry 4207 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN duty_shift_mutex.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_mutex.id IS 'ID';


--
-- TOC entry 4208 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN duty_shift_mutex.shift_config_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_mutex.shift_config_id IS '班次配置ID';


--
-- TOC entry 4209 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN duty_shift_mutex.mutex_shift_config_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_mutex.mutex_shift_config_id IS '互斥班次配置ID';


--
-- TOC entry 4210 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN duty_shift_mutex.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_mutex.create_time IS '创建时间';


--
-- TOC entry 4211 (class 0 OID 0)
-- Dependencies: 245
-- Name: COLUMN duty_shift_mutex.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.duty_shift_mutex.update_time IS '更新时间';


--
-- TOC entry 244 (class 1259 OID 34273)
-- Name: duty_shift_mutex_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.duty_shift_mutex_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.duty_shift_mutex_id_seq OWNER TO postgres;

--
-- TOC entry 4212 (class 0 OID 0)
-- Dependencies: 244
-- Name: duty_shift_mutex_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.duty_shift_mutex_id_seq OWNED BY public.duty_shift_mutex.id;


--
-- TOC entry 247 (class 1259 OID 34299)
-- Name: employee_available_time; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.employee_available_time (
    id bigint NOT NULL,
    employee_id bigint NOT NULL,
    day_of_week smallint NOT NULL,
    start_time time without time zone,
    end_time time without time zone,
    is_available smallint DEFAULT '1'::smallint,
    remark character varying(200) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.employee_available_time OWNER TO postgres;

--
-- TOC entry 4213 (class 0 OID 0)
-- Dependencies: 247
-- Name: TABLE employee_available_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.employee_available_time IS '人员可排班时段表';


--
-- TOC entry 4214 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN employee_available_time.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.employee_available_time.id IS 'ID';


--
-- TOC entry 4215 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN employee_available_time.employee_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.employee_available_time.employee_id IS '员工ID';


--
-- TOC entry 4216 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN employee_available_time.day_of_week; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.employee_available_time.day_of_week IS '星期:1-周一,2-周二,3-周三,4-周四,5-周五,6-周六,7-周日';


--
-- TOC entry 4217 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN employee_available_time.start_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.employee_available_time.start_time IS '开始时间';


--
-- TOC entry 4218 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN employee_available_time.end_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.employee_available_time.end_time IS '结束时间';


--
-- TOC entry 4219 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN employee_available_time.is_available; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.employee_available_time.is_available IS '是否可用:0-否,1-是';


--
-- TOC entry 4220 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN employee_available_time.remark; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.employee_available_time.remark IS '备注';


--
-- TOC entry 4221 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN employee_available_time.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.employee_available_time.create_time IS '创建时间';


--
-- TOC entry 4222 (class 0 OID 0)
-- Dependencies: 247
-- Name: COLUMN employee_available_time.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.employee_available_time.update_time IS '更新时间';


--
-- TOC entry 246 (class 1259 OID 34298)
-- Name: employee_available_time_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.employee_available_time_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.employee_available_time_id_seq OWNER TO postgres;

--
-- TOC entry 4223 (class 0 OID 0)
-- Dependencies: 246
-- Name: employee_available_time_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.employee_available_time_id_seq OWNED BY public.employee_available_time.id;


--
-- TOC entry 257 (class 1259 OID 34431)
-- Name: leave_request; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.leave_request (
    id bigint NOT NULL,
    request_no character varying(50) NOT NULL,
    employee_id bigint NOT NULL,
    leave_type smallint NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    start_time time without time zone,
    end_time time without time zone,
    total_hours numeric(6,2) DEFAULT NULL::numeric,
    reason character varying(500) NOT NULL,
    attachment_url character varying(500) DEFAULT NULL::character varying,
    approval_status character varying(20) DEFAULT 'pending'::character varying,
    current_approver_id bigint,
    approval_level integer DEFAULT 1,
    total_approval_levels integer DEFAULT 1,
    substitute_employee_id bigint,
    substitute_type smallint DEFAULT '1'::smallint,
    substitute_status character varying(20) DEFAULT 'pending'::character varying,
    reject_reason character varying(500) DEFAULT NULL::character varying,
    approval_opinion character varying(500) DEFAULT NULL::character varying,
    schedule_completed smallint DEFAULT '0'::smallint,
    schedule_completed_time timestamp without time zone,
    schedule_completed_by bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    schedule_id bigint,
    shift_config_id bigint,
    shift_config_ids character varying(500) DEFAULT NULL::character varying
);


ALTER TABLE public.leave_request OWNER TO postgres;

--
-- TOC entry 4224 (class 0 OID 0)
-- Dependencies: 257
-- Name: TABLE leave_request; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.leave_request IS '请假申请表';


--
-- TOC entry 4225 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.id IS '请假申请ID';


--
-- TOC entry 4226 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.request_no; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.request_no IS '申请编号';


--
-- TOC entry 4227 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.employee_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.employee_id IS '申请人ID';


--
-- TOC entry 4228 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.leave_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.leave_type IS '请假类型:1-事假,2-病假,3-年假,4-调休,5-其他';


--
-- TOC entry 4229 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.start_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.start_date IS '开始日期';


--
-- TOC entry 4230 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.end_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.end_date IS '结束日期';


--
-- TOC entry 4231 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.start_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.start_time IS '开始时间';


--
-- TOC entry 4232 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.end_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.end_time IS '结束时间';


--
-- TOC entry 4233 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.total_hours; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.total_hours IS '请假总时长(小时)';


--
-- TOC entry 4234 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.reason; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.reason IS '请假原因';


--
-- TOC entry 4235 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.attachment_url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.attachment_url IS '附件URL';


--
-- TOC entry 4236 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.approval_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.approval_status IS '审批状态:pending-待审批,approved-已通过,rejected-已拒绝,cancelled-已取消';


--
-- TOC entry 4237 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.current_approver_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.current_approver_id IS '当前审批人ID';


--
-- TOC entry 4238 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.approval_level; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.approval_level IS '当前审批层级';


--
-- TOC entry 4239 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.total_approval_levels; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.total_approval_levels IS '总审批层级';


--
-- TOC entry 4240 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.substitute_employee_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.substitute_employee_id IS '替补人员ID';


--
-- TOC entry 4241 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.substitute_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.substitute_type IS '替补类型:1-自动匹配,2-手动选择';


--
-- TOC entry 4242 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.substitute_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.substitute_status IS '替补状态:pending-待确认,confirmed-已确认,rejected-已拒绝';


--
-- TOC entry 4243 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.reject_reason; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.reject_reason IS '拒绝原因';


--
-- TOC entry 4244 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.approval_opinion; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.approval_opinion IS '审批意见';


--
-- TOC entry 4245 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.schedule_completed; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.schedule_completed IS '排班是否完成:0-未完成,1-已完成';


--
-- TOC entry 4246 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.schedule_completed_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.schedule_completed_time IS '排班完成时间';


--
-- TOC entry 4247 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.schedule_completed_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.schedule_completed_by IS '排班完成人ID';


--
-- TOC entry 4248 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.create_time IS '创建时间';


--
-- TOC entry 4249 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.update_time IS '更新时间';


--
-- TOC entry 4250 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.schedule_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.schedule_id IS '值班表ID';


--
-- TOC entry 4251 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.shift_config_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.shift_config_id IS '班次配置ID';


--
-- TOC entry 4252 (class 0 OID 0)
-- Dependencies: 257
-- Name: COLUMN leave_request.shift_config_ids; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_request.shift_config_ids IS '班次配置ID列表，多个用逗号分隔';


--
-- TOC entry 256 (class 1259 OID 34430)
-- Name: leave_request_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.leave_request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.leave_request_id_seq OWNER TO postgres;

--
-- TOC entry 4253 (class 0 OID 0)
-- Dependencies: 256
-- Name: leave_request_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.leave_request_id_seq OWNED BY public.leave_request.id;


--
-- TOC entry 269 (class 1259 OID 34662)
-- Name: leave_substitute; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.leave_substitute (
    id bigint NOT NULL,
    leave_request_id bigint NOT NULL,
    original_employee_id bigint NOT NULL,
    substitute_employee_id bigint NOT NULL,
    duty_date date NOT NULL,
    shift_config_id bigint NOT NULL,
    status integer DEFAULT 1,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.leave_substitute OWNER TO postgres;

--
-- TOC entry 4254 (class 0 OID 0)
-- Dependencies: 269
-- Name: TABLE leave_substitute; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.leave_substitute IS '请假顶岗信息表';


--
-- TOC entry 4255 (class 0 OID 0)
-- Dependencies: 269
-- Name: COLUMN leave_substitute.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_substitute.id IS 'ID';


--
-- TOC entry 4256 (class 0 OID 0)
-- Dependencies: 269
-- Name: COLUMN leave_substitute.leave_request_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_substitute.leave_request_id IS '请假申请ID';


--
-- TOC entry 4257 (class 0 OID 0)
-- Dependencies: 269
-- Name: COLUMN leave_substitute.original_employee_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_substitute.original_employee_id IS '原值班人员ID';


--
-- TOC entry 4258 (class 0 OID 0)
-- Dependencies: 269
-- Name: COLUMN leave_substitute.substitute_employee_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_substitute.substitute_employee_id IS '顶岗人员ID';


--
-- TOC entry 4259 (class 0 OID 0)
-- Dependencies: 269
-- Name: COLUMN leave_substitute.duty_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_substitute.duty_date IS '值班日期';


--
-- TOC entry 4260 (class 0 OID 0)
-- Dependencies: 269
-- Name: COLUMN leave_substitute.shift_config_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_substitute.shift_config_id IS '班次配置ID';


--
-- TOC entry 4261 (class 0 OID 0)
-- Dependencies: 269
-- Name: COLUMN leave_substitute.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_substitute.status IS '状态';


--
-- TOC entry 4262 (class 0 OID 0)
-- Dependencies: 269
-- Name: COLUMN leave_substitute.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_substitute.create_time IS '创建时间';


--
-- TOC entry 4263 (class 0 OID 0)
-- Dependencies: 269
-- Name: COLUMN leave_substitute.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.leave_substitute.update_time IS '更新时间';


--
-- TOC entry 268 (class 1259 OID 34661)
-- Name: leave_substitute_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.leave_substitute_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.leave_substitute_id_seq OWNER TO postgres;

--
-- TOC entry 4264 (class 0 OID 0)
-- Dependencies: 268
-- Name: leave_substitute_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.leave_substitute_id_seq OWNED BY public.leave_substitute.id;


--
-- TOC entry 259 (class 1259 OID 34490)
-- Name: operation_log; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.operation_log (
    id bigint NOT NULL,
    operator_id bigint,
    operator_name character varying(50) DEFAULT NULL::character varying,
    operation_type character varying(50) NOT NULL,
    operation_module character varying(50) NOT NULL,
    operation_desc character varying(500) DEFAULT NULL::character varying,
    request_method character varying(10) DEFAULT NULL::character varying,
    request_url character varying(500) DEFAULT NULL::character varying,
    request_params text,
    response_result text,
    ip_address character varying(50) DEFAULT NULL::character varying,
    user_agent character varying(500) DEFAULT NULL::character varying,
    execution_time integer,
    status smallint DEFAULT '1'::smallint,
    error_msg text,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.operation_log OWNER TO postgres;

--
-- TOC entry 4265 (class 0 OID 0)
-- Dependencies: 259
-- Name: TABLE operation_log; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.operation_log IS '操作日志表';


--
-- TOC entry 4266 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.id IS '日志ID';


--
-- TOC entry 4267 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.operator_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.operator_id IS '操作人ID';


--
-- TOC entry 4268 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.operator_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.operator_name IS '操作人姓名';


--
-- TOC entry 4269 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.operation_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.operation_type IS '操作类型';


--
-- TOC entry 4270 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.operation_module; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.operation_module IS '操作模块';


--
-- TOC entry 4271 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.operation_desc; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.operation_desc IS '操作描述';


--
-- TOC entry 4272 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.request_method; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.request_method IS '请求方法';


--
-- TOC entry 4273 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.request_url; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.request_url IS '请求URL';


--
-- TOC entry 4274 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.request_params; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.request_params IS '请求参数';


--
-- TOC entry 4275 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.response_result; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.response_result IS '响应结果';


--
-- TOC entry 4276 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.ip_address; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.ip_address IS 'IP地址';


--
-- TOC entry 4277 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.user_agent; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.user_agent IS '用户代理';


--
-- TOC entry 4278 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.execution_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.execution_time IS '执行时间(毫秒)';


--
-- TOC entry 4279 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.status IS '状态:0-失败,1-成功';


--
-- TOC entry 4280 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.error_msg; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.error_msg IS '错误信息';


--
-- TOC entry 4281 (class 0 OID 0)
-- Dependencies: 259
-- Name: COLUMN operation_log.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.operation_log.create_time IS '创建时间';


--
-- TOC entry 258 (class 1259 OID 34489)
-- Name: operation_log_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.operation_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.operation_log_id_seq OWNER TO postgres;

--
-- TOC entry 4282 (class 0 OID 0)
-- Dependencies: 258
-- Name: operation_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.operation_log_id_seq OWNED BY public.operation_log.id;


--
-- TOC entry 275 (class 1259 OID 34774)
-- Name: pm_project; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.pm_project (
    id bigint NOT NULL,
    project_name character varying(100) NOT NULL,
    project_code character varying(50) NOT NULL,
    module_id bigint,
    priority integer DEFAULT 3,
    status integer DEFAULT 1,
    progress integer DEFAULT 0,
    start_date date,
    end_date date,
    description character varying(200) DEFAULT NULL::character varying,
    owner_id bigint,
    create_by bigint,
    archived integer DEFAULT 0,
    sort integer DEFAULT 0,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.pm_project OWNER TO postgres;

--
-- TOC entry 4283 (class 0 OID 0)
-- Dependencies: 275
-- Name: TABLE pm_project; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.pm_project IS '项目表';


--
-- TOC entry 4284 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.id IS '项目ID';


--
-- TOC entry 4285 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.project_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.project_name IS '项目名称';


--
-- TOC entry 4286 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.project_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.project_code IS '项目编码';


--
-- TOC entry 4287 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.module_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.module_id IS '模块ID';


--
-- TOC entry 4288 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.priority; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.priority IS '优先级：1-高，2-中，3-低';


--
-- TOC entry 4289 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.status IS '状态：1-进行中，2-已完成，3-已暂停，4-已取消';


--
-- TOC entry 4290 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.progress; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.progress IS '进度百分比';


--
-- TOC entry 4291 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.start_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.start_date IS '开始日期';


--
-- TOC entry 4292 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.end_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.end_date IS '结束日期';


--
-- TOC entry 4293 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.description IS '项目描述';


--
-- TOC entry 4294 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.owner_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.owner_id IS '负责人ID';


--
-- TOC entry 4295 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.create_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.create_by IS '创建人ID';


--
-- TOC entry 4296 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.archived; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.archived IS '是否归档：0-否，1-是';


--
-- TOC entry 4296 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.sort; Type: COMMENT; Schema: public; Owner: postgres
--


COMMENT ON COLUMN public.pm_project.sort IS '排序';


--
-- TOC entry 4297 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.create_time IS '创建时间';


--
-- TOC entry 4298 (class 0 OID 0)
-- Dependencies: 275
-- Name: COLUMN pm_project.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_project.update_time IS '更新时间';


--
-- TOC entry 279 (class 1259 OID 57346)
-- Name: pm_project_employee; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.pm_project_employee (
    id bigint NOT NULL,
    project_id bigint NOT NULL,
    employee_id bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.pm_project_employee OWNER TO postgres;

--
-- TOC entry 278 (class 1259 OID 57345)
-- Name: pm_project_employee_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.pm_project_employee_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.pm_project_employee_id_seq OWNER TO postgres;

--
-- TOC entry 4299 (class 0 OID 0)
-- Dependencies: 278
-- Name: pm_project_employee_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.pm_project_employee_id_seq OWNED BY public.pm_project_employee.id;


--
-- TOC entry 274 (class 1259 OID 34773)
-- Name: pm_project_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.pm_project_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.pm_project_id_seq OWNER TO postgres;

--
-- TOC entry 4300 (class 0 OID 0)
-- Dependencies: 274
-- Name: pm_project_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.pm_project_id_seq OWNED BY public.pm_project.id;


--
-- TOC entry 277 (class 1259 OID 34794)
-- Name: pm_task; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.pm_task (
    id bigint NOT NULL,
    task_name character varying(100) NOT NULL,
    task_code character varying(50) NOT NULL,
    project_id bigint NOT NULL,
    description character varying(200) DEFAULT NULL::character varying,
    start_date date,
    end_date date,
    status smallint DEFAULT '1'::smallint,
    priority smallint DEFAULT '3'::smallint,
    assignee_id bigint,
    create_by bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    parent_id bigint DEFAULT 0,
    task_level integer DEFAULT 1,
    progress integer DEFAULT 0,
    is_pinned integer DEFAULT 0,
    module_id bigint
);


ALTER TABLE public.pm_task OWNER TO postgres;

--
-- TOC entry 4301 (class 0 OID 0)
-- Dependencies: 277
-- Name: TABLE pm_task; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.pm_task IS '任务表';


--
-- TOC entry 4302 (class 0 OID 0)
-- Dependencies: 277
-- Name: COLUMN pm_task.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_task.id IS '任务ID';


--
-- TOC entry 4303 (class 0 OID 0)
-- Dependencies: 277
-- Name: COLUMN pm_task.task_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_task.task_name IS '任务名称';


--
-- TOC entry 4304 (class 0 OID 0)
-- Dependencies: 277
-- Name: COLUMN pm_task.task_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_task.task_code IS '任务编码';


--
-- TOC entry 4305 (class 0 OID 0)
-- Dependencies: 277
-- Name: COLUMN pm_task.project_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_task.project_id IS '项目ID';


--
-- TOC entry 4306 (class 0 OID 0)
-- Dependencies: 277
-- Name: COLUMN pm_task.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_task.description IS '任务描述';


--
-- TOC entry 4307 (class 0 OID 0)
-- Dependencies: 277
-- Name: COLUMN pm_task.start_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_task.start_date IS '开始日期';


--
-- TOC entry 4308 (class 0 OID 0)
-- Dependencies: 277
-- Name: COLUMN pm_task.end_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_task.end_date IS '结束日期';


--
-- TOC entry 4309 (class 0 OID 0)
-- Dependencies: 277
-- Name: COLUMN pm_task.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_task.status IS '状态：1-未开始，2-进行中，3-已完成，4-已暂停，5-已取消';


--
-- TOC entry 4310 (class 0 OID 0)
-- Dependencies: 277
-- Name: COLUMN pm_task.priority; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_task.priority IS '优先级：1-高，2-中，3-低';


--
-- TOC entry 4311 (class 0 OID 0)
-- Dependencies: 277
-- Name: COLUMN pm_task.assignee_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_task.assignee_id IS '负责人ID';


--
-- TOC entry 4312 (class 0 OID 0)
-- Dependencies: 277
-- Name: COLUMN pm_task.create_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_task.create_by IS '创建人ID';


--
-- TOC entry 4313 (class 0 OID 0)
-- Dependencies: 277
-- Name: COLUMN pm_task.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_task.create_time IS '创建时间';


--
-- TOC entry 4314 (class 0 OID 0)
-- Dependencies: 277
-- Name: COLUMN pm_task.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.pm_task.update_time IS '更新时间';


--
-- TOC entry 283 (class 1259 OID 57396)
-- Name: pm_task_comment; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.pm_task_comment (
    id bigint NOT NULL,
    task_id bigint NOT NULL,
    employee_id bigint NOT NULL,
    content text,
    attachment_url character varying(255),
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.pm_task_comment OWNER TO postgres;

--
-- TOC entry 285 (class 1259 OID 57420)
-- Name: pm_task_comment_attachment; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.pm_task_comment_attachment (
    id bigint NOT NULL,
    comment_id bigint NOT NULL,
    file_name character varying(255) NOT NULL,
    file_path character varying(255) NOT NULL,
    file_size bigint NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.pm_task_comment_attachment OWNER TO postgres;

--
-- TOC entry 284 (class 1259 OID 57419)
-- Name: pm_task_comment_attachment_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.pm_task_comment_attachment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.pm_task_comment_attachment_id_seq OWNER TO postgres;

--
-- TOC entry 4315 (class 0 OID 0)
-- Dependencies: 284
-- Name: pm_task_comment_attachment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.pm_task_comment_attachment_id_seq OWNED BY public.pm_task_comment_attachment.id;


--
-- TOC entry 282 (class 1259 OID 57395)
-- Name: pm_task_comment_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.pm_task_comment_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.pm_task_comment_id_seq OWNER TO postgres;

--
-- TOC entry 4316 (class 0 OID 0)
-- Dependencies: 282
-- Name: pm_task_comment_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.pm_task_comment_id_seq OWNED BY public.pm_task_comment.id;


--
-- TOC entry 276 (class 1259 OID 34793)
-- Name: pm_task_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.pm_task_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.pm_task_id_seq OWNER TO postgres;

--
-- TOC entry 4317 (class 0 OID 0)
-- Dependencies: 276
-- Name: pm_task_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.pm_task_id_seq OWNED BY public.pm_task.id;


--
-- TOC entry 281 (class 1259 OID 57370)
-- Name: pm_task_stakeholder; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.pm_task_stakeholder (
    id bigint NOT NULL,
    task_id bigint NOT NULL,
    employee_id bigint NOT NULL,
    stakeholder_type smallint DEFAULT 1 NOT NULL,
    created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    updated_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.pm_task_stakeholder OWNER TO postgres;

--
-- TOC entry 280 (class 1259 OID 57369)
-- Name: pm_task_stakeholder_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.pm_task_stakeholder_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.pm_task_stakeholder_id_seq OWNER TO postgres;

--
-- TOC entry 4318 (class 0 OID 0)
-- Dependencies: 280
-- Name: pm_task_stakeholder_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.pm_task_stakeholder_id_seq OWNED BY public.pm_task_stakeholder.id;


--
-- TOC entry 253 (class 1259 OID 34369)
-- Name: schedule_version; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.schedule_version (
    id bigint NOT NULL,
    schedule_id bigint NOT NULL,
    version_name character varying(100) NOT NULL,
    version_code character varying(50) NOT NULL,
    start_date date NOT NULL,
    end_date date NOT NULL,
    status character varying(20) DEFAULT 'draft'::character varying,
    is_current smallint DEFAULT '0'::smallint,
    create_by bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    publish_time timestamp without time zone
);


ALTER TABLE public.schedule_version OWNER TO postgres;

--
-- TOC entry 4319 (class 0 OID 0)
-- Dependencies: 253
-- Name: TABLE schedule_version; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.schedule_version IS '排班版本表';


--
-- TOC entry 4320 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN schedule_version.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.schedule_version.id IS '版本ID';


--
-- TOC entry 4321 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN schedule_version.schedule_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.schedule_version.schedule_id IS '值班表ID';


--
-- TOC entry 4322 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN schedule_version.version_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.schedule_version.version_name IS '版本名称';


--
-- TOC entry 4323 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN schedule_version.version_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.schedule_version.version_code IS '版本编码';


--
-- TOC entry 4324 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN schedule_version.start_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.schedule_version.start_date IS '开始日期';


--
-- TOC entry 4325 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN schedule_version.end_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.schedule_version.end_date IS '结束日期';


--
-- TOC entry 4326 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN schedule_version.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.schedule_version.status IS '状态:draft-草稿,published-已发布,archived-已归档';


--
-- TOC entry 4327 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN schedule_version.is_current; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.schedule_version.is_current IS '是否当前版本:0-否,1-是';


--
-- TOC entry 4328 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN schedule_version.create_by; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.schedule_version.create_by IS '创建人ID';


--
-- TOC entry 4329 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN schedule_version.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.schedule_version.create_time IS '创建时间';


--
-- TOC entry 4330 (class 0 OID 0)
-- Dependencies: 253
-- Name: COLUMN schedule_version.publish_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.schedule_version.publish_time IS '发布时间';


--
-- TOC entry 252 (class 1259 OID 34368)
-- Name: schedule_version_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.schedule_version_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.schedule_version_id_seq OWNER TO postgres;

--
-- TOC entry 4331 (class 0 OID 0)
-- Dependencies: 252
-- Name: schedule_version_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.schedule_version_id_seq OWNED BY public.schedule_version.id;


--
-- TOC entry 271 (class 1259 OID 34700)
-- Name: swap_request; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.swap_request (
    id bigint NOT NULL,
    request_no character varying(50) NOT NULL,
    original_employee_id bigint NOT NULL,
    target_employee_id bigint NOT NULL,
    original_assignment_id bigint NOT NULL,
    target_assignment_id bigint,
    schedule_id bigint NOT NULL,
    swap_date date NOT NULL,
    swap_shift integer NOT NULL,
    reason character varying(500) DEFAULT NULL::character varying,
    approval_status character varying(20) DEFAULT 'pending'::character varying,
    current_approver_id bigint,
    approval_level integer DEFAULT 1,
    total_approval_levels integer DEFAULT 1,
    original_confirm_status character varying(20) DEFAULT 'pending'::character varying,
    target_confirm_status character varying(20) DEFAULT 'pending'::character varying,
    reject_reason character varying(500) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    original_swap_date date,
    original_swap_shift integer,
    target_swap_date date,
    target_swap_shift integer
);


ALTER TABLE public.swap_request OWNER TO postgres;

--
-- TOC entry 4332 (class 0 OID 0)
-- Dependencies: 271
-- Name: TABLE swap_request; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.swap_request IS '调班记录表';


--
-- TOC entry 4333 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.id IS '调班申请ID';


--
-- TOC entry 4334 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.request_no; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.request_no IS '申请编号';


--
-- TOC entry 4335 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.original_employee_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.original_employee_id IS '原值班人员ID';


--
-- TOC entry 4336 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.target_employee_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.target_employee_id IS '目标值班人员ID';


--
-- TOC entry 4337 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.original_assignment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.original_assignment_id IS '原值班安排ID';


--
-- TOC entry 4338 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.target_assignment_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.target_assignment_id IS '目标值班安排ID';


--
-- TOC entry 4339 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.schedule_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.schedule_id IS '值班表ID';


--
-- TOC entry 4340 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.swap_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.swap_date IS '调班日期';


--
-- TOC entry 4341 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.swap_shift; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.swap_shift IS '调班班次';


--
-- TOC entry 4342 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.reason; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.reason IS '调班原因';


--
-- TOC entry 4343 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.approval_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.approval_status IS '审批状态:pending-待审批,approved-已通过,rejected-已拒绝,cancelled-已取消';


--
-- TOC entry 4344 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.current_approver_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.current_approver_id IS '当前审批人ID';


--
-- TOC entry 4345 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.approval_level; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.approval_level IS '当前审批层级';


--
-- TOC entry 4346 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.total_approval_levels; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.total_approval_levels IS '总审批层级';


--
-- TOC entry 4347 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.original_confirm_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.original_confirm_status IS '原人员确认状态';


--
-- TOC entry 4348 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.target_confirm_status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.target_confirm_status IS '目标人员确认状态';


--
-- TOC entry 4349 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.reject_reason; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.reject_reason IS '拒绝原因';


--
-- TOC entry 4350 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.create_time IS '创建时间';


--
-- TOC entry 4351 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.update_time IS '更新时间';


--
-- TOC entry 4352 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.original_swap_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.original_swap_date IS '原值班日期';


--
-- TOC entry 4353 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.original_swap_shift; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.original_swap_shift IS '原值班班次';


--
-- TOC entry 4354 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.target_swap_date; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.target_swap_date IS '目标值班日期';


--
-- TOC entry 4355 (class 0 OID 0)
-- Dependencies: 271
-- Name: COLUMN swap_request.target_swap_shift; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.swap_request.target_swap_shift IS '目标值班班次';


--
-- TOC entry 270 (class 1259 OID 34699)
-- Name: swap_request_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.swap_request_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.swap_request_id_seq OWNER TO postgres;

--
-- TOC entry 4356 (class 0 OID 0)
-- Dependencies: 270
-- Name: swap_request_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.swap_request_id_seq OWNED BY public.swap_request.id;


--
-- TOC entry 221 (class 1259 OID 33997)
-- Name: sys_dept; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_dept (
    id bigint NOT NULL,
    dept_name character varying(50) NOT NULL,
    parent_id bigint DEFAULT '0'::bigint,
    dept_code character varying(20) NOT NULL,
    sort integer DEFAULT 0,
    status smallint DEFAULT '1'::smallint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.sys_dept OWNER TO postgres;

--
-- TOC entry 4357 (class 0 OID 0)
-- Dependencies: 221
-- Name: TABLE sys_dept; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.sys_dept IS '部门表';


--
-- TOC entry 4358 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN sys_dept.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.id IS '部门ID';


--
-- TOC entry 4359 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN sys_dept.dept_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.dept_name IS '部门名称';


--
-- TOC entry 4360 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN sys_dept.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.parent_id IS '上级部门ID';


--
-- TOC entry 4361 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN sys_dept.dept_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.dept_code IS '部门编码';


--
-- TOC entry 4362 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN sys_dept.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.sort IS '排序';


--
-- TOC entry 4363 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN sys_dept.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.status IS '状态：0禁用，1启用';


--
-- TOC entry 4364 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN sys_dept.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.create_time IS '创建时间';


--
-- TOC entry 4365 (class 0 OID 0)
-- Dependencies: 221
-- Name: COLUMN sys_dept.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dept.update_time IS '更新时间';


--
-- TOC entry 220 (class 1259 OID 33996)
-- Name: sys_dept_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sys_dept_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_dept_id_seq OWNER TO postgres;

--
-- TOC entry 4366 (class 0 OID 0)
-- Dependencies: 220
-- Name: sys_dept_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sys_dept_id_seq OWNED BY public.sys_dept.id;


--
-- TOC entry 241 (class 1259 OID 34226)
-- Name: sys_dict_data; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_dict_data (
    id bigint NOT NULL,
    dict_type_id bigint NOT NULL,
    dict_label character varying(100) NOT NULL,
    dict_value character varying(100) NOT NULL,
    dict_sort integer DEFAULT 0,
    css_class character varying(100) DEFAULT NULL::character varying,
    list_class character varying(100) DEFAULT NULL::character varying,
    is_default smallint DEFAULT '0'::smallint,
    status smallint DEFAULT '1'::smallint,
    remark character varying(500) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.sys_dict_data OWNER TO postgres;

--
-- TOC entry 4367 (class 0 OID 0)
-- Dependencies: 241
-- Name: TABLE sys_dict_data; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.sys_dict_data IS '字典数据表';


--
-- TOC entry 4368 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dict_data.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_data.id IS '字典数据ID';


--
-- TOC entry 4369 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dict_data.dict_type_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_data.dict_type_id IS '字典类型ID';


--
-- TOC entry 4370 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dict_data.dict_label; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_data.dict_label IS '字典标签';


--
-- TOC entry 4371 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dict_data.dict_value; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_data.dict_value IS '字典键值';


--
-- TOC entry 4372 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dict_data.dict_sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_data.dict_sort IS '字典排序';


--
-- TOC entry 4373 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dict_data.css_class; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_data.css_class IS '样式属性（其他样式扩展）';


--
-- TOC entry 4374 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dict_data.list_class; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_data.list_class IS '表格回显样式';


--
-- TOC entry 4375 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dict_data.is_default; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_data.is_default IS '是否默认：0否，1是';


--
-- TOC entry 4376 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dict_data.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_data.status IS '状态：0禁用，1启用';


--
-- TOC entry 4377 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dict_data.remark; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_data.remark IS '备注';


--
-- TOC entry 4378 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dict_data.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_data.create_time IS '创建时间';


--
-- TOC entry 4379 (class 0 OID 0)
-- Dependencies: 241
-- Name: COLUMN sys_dict_data.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_data.update_time IS '更新时间';


--
-- TOC entry 240 (class 1259 OID 34225)
-- Name: sys_dict_data_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sys_dict_data_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_dict_data_id_seq OWNER TO postgres;

--
-- TOC entry 4380 (class 0 OID 0)
-- Dependencies: 240
-- Name: sys_dict_data_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sys_dict_data_id_seq OWNED BY public.sys_dict_data.id;


--
-- TOC entry 223 (class 1259 OID 34013)
-- Name: sys_dict_type; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_dict_type (
    id bigint NOT NULL,
    dict_name character varying(100) NOT NULL,
    dict_code character varying(100) NOT NULL,
    description character varying(200) DEFAULT NULL::character varying,
    status smallint DEFAULT '1'::smallint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.sys_dict_type OWNER TO postgres;

--
-- TOC entry 4381 (class 0 OID 0)
-- Dependencies: 223
-- Name: TABLE sys_dict_type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.sys_dict_type IS '字典类型表';


--
-- TOC entry 4382 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN sys_dict_type.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_type.id IS '字典类型ID';


--
-- TOC entry 4383 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN sys_dict_type.dict_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_type.dict_name IS '字典名称';


--
-- TOC entry 4384 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN sys_dict_type.dict_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_type.dict_code IS '字典编码';


--
-- TOC entry 4385 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN sys_dict_type.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_type.description IS '字典描述';


--
-- TOC entry 4386 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN sys_dict_type.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_type.status IS '状态：0禁用，1启用';


--
-- TOC entry 4387 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN sys_dict_type.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_type.create_time IS '创建时间';


--
-- TOC entry 4388 (class 0 OID 0)
-- Dependencies: 223
-- Name: COLUMN sys_dict_type.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_dict_type.update_time IS '更新时间';


--
-- TOC entry 222 (class 1259 OID 34012)
-- Name: sys_dict_type_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sys_dict_type_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_dict_type_id_seq OWNER TO postgres;

--
-- TOC entry 4389 (class 0 OID 0)
-- Dependencies: 222
-- Name: sys_dict_type_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sys_dict_type_id_seq OWNED BY public.sys_dict_type.id;


--
-- TOC entry 239 (class 1259 OID 34198)
-- Name: sys_employee; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_employee (
    id bigint NOT NULL,
    employee_name character varying(50) NOT NULL,
    dept_id bigint NOT NULL,
    employee_code character varying(20) DEFAULT NULL::character varying,
    phone character varying(11) DEFAULT NULL::character varying,
    email character varying(50) DEFAULT NULL::character varying,
    gender smallint DEFAULT '0'::smallint,
    dict_type_id bigint,
    dict_data_id bigint,
    status smallint DEFAULT '1'::smallint,
    username character varying(50) DEFAULT NULL::character varying,
    password character varying(100) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    sort integer DEFAULT 0
);


ALTER TABLE public.sys_employee OWNER TO postgres;

--
-- TOC entry 4390 (class 0 OID 0)
-- Dependencies: 239
-- Name: TABLE sys_employee; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.sys_employee IS '人员表';


--
-- TOC entry 4391 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_employee.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_employee.id IS '人员ID';


--
-- TOC entry 4392 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_employee.employee_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_employee.employee_name IS '人员姓名';


--
-- TOC entry 4393 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_employee.dept_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_employee.dept_id IS '所属部门ID';


--
-- TOC entry 4394 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_employee.employee_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_employee.employee_code IS '人员编码';


--
-- TOC entry 4395 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_employee.phone; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_employee.phone IS '手机号码';


--
-- TOC entry 4396 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_employee.email; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_employee.email IS '邮箱';


--
-- TOC entry 4397 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_employee.gender; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_employee.gender IS '性别：0未知，1男，2女';


--
-- TOC entry 4398 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_employee.dict_type_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_employee.dict_type_id IS '字典类型ID';


--
-- TOC entry 4399 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_employee.dict_data_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_employee.dict_data_id IS '字典数据ID';


--
-- TOC entry 4400 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_employee.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_employee.status IS '状态：0禁用，1启用';


--
-- TOC entry 4401 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_employee.username; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_employee.username IS '用户名';


--
-- TOC entry 4402 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_employee.password; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_employee.password IS '密码';


--
-- TOC entry 4403 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_employee.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_employee.create_time IS '创建时间';


--
-- TOC entry 4404 (class 0 OID 0)
-- Dependencies: 239
-- Name: COLUMN sys_employee.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_employee.update_time IS '更新时间';


--
-- TOC entry 238 (class 1259 OID 34197)
-- Name: sys_employee_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sys_employee_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_employee_id_seq OWNER TO postgres;

--
-- TOC entry 4405 (class 0 OID 0)
-- Dependencies: 238
-- Name: sys_employee_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sys_employee_id_seq OWNED BY public.sys_employee.id;


--
-- TOC entry 225 (class 1259 OID 34030)
-- Name: sys_menu; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_menu (
    id bigint NOT NULL,
    menu_name character varying(50) NOT NULL,
    parent_id bigint DEFAULT '0'::bigint,
    path character varying(200) DEFAULT NULL::character varying,
    component character varying(200) DEFAULT NULL::character varying,
    perm character varying(100) DEFAULT NULL::character varying,
    type smallint NOT NULL,
    icon character varying(50) DEFAULT NULL::character varying,
    sort integer DEFAULT 0,
    status smallint DEFAULT '1'::smallint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.sys_menu OWNER TO postgres;

--
-- TOC entry 4406 (class 0 OID 0)
-- Dependencies: 225
-- Name: TABLE sys_menu; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.sys_menu IS '菜单表';


--
-- TOC entry 4407 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN sys_menu.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_menu.id IS '菜单ID';


--
-- TOC entry 4408 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN sys_menu.menu_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_menu.menu_name IS '菜单名称';


--
-- TOC entry 4409 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN sys_menu.parent_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_menu.parent_id IS '父菜单ID';


--
-- TOC entry 4410 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN sys_menu.path; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_menu.path IS '路由路径';


--
-- TOC entry 4411 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN sys_menu.component; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_menu.component IS '组件路径';


--
-- TOC entry 4412 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN sys_menu.perm; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_menu.perm IS '权限标识';


--
-- TOC entry 4413 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN sys_menu.type; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_menu.type IS '菜单类型：1目录，2菜单，3按钮';


--
-- TOC entry 4414 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN sys_menu.icon; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_menu.icon IS '菜单图标';


--
-- TOC entry 4415 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN sys_menu.sort; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_menu.sort IS '排序';


--
-- TOC entry 4416 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN sys_menu.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_menu.status IS '状态：0禁用，1启用';


--
-- TOC entry 4417 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN sys_menu.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_menu.create_time IS '创建时间';


--
-- TOC entry 4418 (class 0 OID 0)
-- Dependencies: 225
-- Name: COLUMN sys_menu.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_menu.update_time IS '更新时间';


--
-- TOC entry 224 (class 1259 OID 34029)
-- Name: sys_menu_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sys_menu_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_menu_id_seq OWNER TO postgres;

--
-- TOC entry 4419 (class 0 OID 0)
-- Dependencies: 224
-- Name: sys_menu_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sys_menu_id_seq OWNED BY public.sys_menu.id;


--
-- TOC entry 227 (class 1259 OID 34053)
-- Name: sys_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_role (
    id bigint NOT NULL,
    role_name character varying(50) NOT NULL,
    role_code character varying(50) NOT NULL,
    description character varying(200) DEFAULT NULL::character varying,
    status smallint DEFAULT '1'::smallint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    sort integer DEFAULT 0
);


ALTER TABLE public.sys_role OWNER TO postgres;

--
-- TOC entry 4420 (class 0 OID 0)
-- Dependencies: 227
-- Name: TABLE sys_role; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.sys_role IS '角色表';


--
-- TOC entry 4421 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN sys_role.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.id IS '角色ID';


--
-- TOC entry 4422 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN sys_role.role_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.role_name IS '角色名称';


--
-- TOC entry 4423 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN sys_role.role_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.role_code IS '角色编码';


--
-- TOC entry 4424 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN sys_role.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.description IS '角色描述';


--
-- TOC entry 4425 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN sys_role.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.status IS '状态：0禁用，1启用';


--
-- TOC entry 4426 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN sys_role.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.create_time IS '创建时间';


--
-- TOC entry 4427 (class 0 OID 0)
-- Dependencies: 227
-- Name: COLUMN sys_role.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role.update_time IS '更新时间';


--
-- TOC entry 226 (class 1259 OID 34052)
-- Name: sys_role_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sys_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_role_id_seq OWNER TO postgres;

--
-- TOC entry 4428 (class 0 OID 0)
-- Dependencies: 226
-- Name: sys_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sys_role_id_seq OWNED BY public.sys_role.id;


--
-- TOC entry 261 (class 1259 OID 34549)
-- Name: sys_role_menu; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_role_menu (
    id bigint NOT NULL,
    role_id bigint NOT NULL,
    menu_id bigint NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.sys_role_menu OWNER TO postgres;

--
-- TOC entry 4429 (class 0 OID 0)
-- Dependencies: 261
-- Name: TABLE sys_role_menu; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.sys_role_menu IS '角色菜单关联表';


--
-- TOC entry 4430 (class 0 OID 0)
-- Dependencies: 261
-- Name: COLUMN sys_role_menu.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role_menu.id IS 'ID';


--
-- TOC entry 4431 (class 0 OID 0)
-- Dependencies: 261
-- Name: COLUMN sys_role_menu.role_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role_menu.role_id IS '角色ID';


--
-- TOC entry 4432 (class 0 OID 0)
-- Dependencies: 261
-- Name: COLUMN sys_role_menu.menu_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role_menu.menu_id IS '菜单ID';


--
-- TOC entry 4433 (class 0 OID 0)
-- Dependencies: 261
-- Name: COLUMN sys_role_menu.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_role_menu.create_time IS '创建时间';


--
-- TOC entry 260 (class 1259 OID 34548)
-- Name: sys_role_menu_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sys_role_menu_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_role_menu_id_seq OWNER TO postgres;

--
-- TOC entry 4434 (class 0 OID 0)
-- Dependencies: 260
-- Name: sys_role_menu_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sys_role_menu_id_seq OWNED BY public.sys_role_menu.id;


--
-- TOC entry 237 (class 1259 OID 34174)
-- Name: sys_schedule_job; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_schedule_job (
    id bigint NOT NULL,
    job_name character varying(100) NOT NULL,
    job_group character varying(50) NOT NULL,
    job_code character varying(100) NOT NULL,
    cron_expression character varying(100) NOT NULL,
    bean_name character varying(200) DEFAULT NULL::character varying,
    method_name character varying(100) DEFAULT NULL::character varying,
    params text,
    status smallint DEFAULT '1'::smallint,
    concurrent smallint DEFAULT '0'::smallint,
    description character varying(500) DEFAULT NULL::character varying,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.sys_schedule_job OWNER TO postgres;

--
-- TOC entry 4435 (class 0 OID 0)
-- Dependencies: 237
-- Name: TABLE sys_schedule_job; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.sys_schedule_job IS '定时任务表';


--
-- TOC entry 4436 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_schedule_job.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_job.id IS '任务ID';


--
-- TOC entry 4437 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_schedule_job.job_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_job.job_name IS '任务名称';


--
-- TOC entry 4438 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_schedule_job.job_group; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_job.job_group IS '任务分组';


--
-- TOC entry 4439 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_schedule_job.job_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_job.job_code IS '任务编码';


--
-- TOC entry 4440 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_schedule_job.cron_expression; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_job.cron_expression IS 'Cron表达式';


--
-- TOC entry 4441 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_schedule_job.bean_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_job.bean_name IS 'Bean名称';


--
-- TOC entry 4442 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_schedule_job.method_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_job.method_name IS '方法名称';


--
-- TOC entry 4443 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_schedule_job.params; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_job.params IS '参数';


--
-- TOC entry 4444 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_schedule_job.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_job.status IS '状态:0-暂停,1-启用';


--
-- TOC entry 4445 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_schedule_job.concurrent; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_job.concurrent IS '是否允许并发:0-不允许,1-允许';


--
-- TOC entry 4446 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_schedule_job.description; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_job.description IS '任务描述';


--
-- TOC entry 4447 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_schedule_job.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_job.create_time IS '创建时间';


--
-- TOC entry 4448 (class 0 OID 0)
-- Dependencies: 237
-- Name: COLUMN sys_schedule_job.update_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_job.update_time IS '更新时间';


--
-- TOC entry 236 (class 1259 OID 34173)
-- Name: sys_schedule_job_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sys_schedule_job_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_schedule_job_id_seq OWNER TO postgres;

--
-- TOC entry 4449 (class 0 OID 0)
-- Dependencies: 236
-- Name: sys_schedule_job_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sys_schedule_job_id_seq OWNED BY public.sys_schedule_job.id;


--
-- TOC entry 273 (class 1259 OID 34756)
-- Name: sys_schedule_log; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_schedule_log (
    id bigint NOT NULL,
    job_name character varying(100) NOT NULL,
    job_group character varying(50) NOT NULL,
    status smallint NOT NULL,
    error_msg text,
    start_time timestamp without time zone,
    end_time timestamp without time zone,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    job_id bigint NOT NULL,
    job_code character varying(100) NOT NULL,
    params text,
    execute_time bigint
);


ALTER TABLE public.sys_schedule_log OWNER TO postgres;

--
-- TOC entry 4450 (class 0 OID 0)
-- Dependencies: 273
-- Name: TABLE sys_schedule_log; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.sys_schedule_log IS '定时任务日志表';


--
-- TOC entry 4451 (class 0 OID 0)
-- Dependencies: 273
-- Name: COLUMN sys_schedule_log.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_log.id IS '日志ID';


--
-- TOC entry 4452 (class 0 OID 0)
-- Dependencies: 273
-- Name: COLUMN sys_schedule_log.job_name; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_log.job_name IS '任务名称';


--
-- TOC entry 4453 (class 0 OID 0)
-- Dependencies: 273
-- Name: COLUMN sys_schedule_log.job_group; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_log.job_group IS '任务分组';


--
-- TOC entry 4454 (class 0 OID 0)
-- Dependencies: 273
-- Name: COLUMN sys_schedule_log.status; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_log.status IS '执行状态:0-失败,1-成功';


--
-- TOC entry 4455 (class 0 OID 0)
-- Dependencies: 273
-- Name: COLUMN sys_schedule_log.error_msg; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_log.error_msg IS '错误信息';


--
-- TOC entry 4456 (class 0 OID 0)
-- Dependencies: 273
-- Name: COLUMN sys_schedule_log.start_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_log.start_time IS '开始时间';


--
-- TOC entry 4457 (class 0 OID 0)
-- Dependencies: 273
-- Name: COLUMN sys_schedule_log.end_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_log.end_time IS '结束时间';


--
-- TOC entry 4458 (class 0 OID 0)
-- Dependencies: 273
-- Name: COLUMN sys_schedule_log.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_log.create_time IS '创建时间';


--
-- TOC entry 4459 (class 0 OID 0)
-- Dependencies: 273
-- Name: COLUMN sys_schedule_log.job_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_log.job_id IS '任务ID';


--
-- TOC entry 4460 (class 0 OID 0)
-- Dependencies: 273
-- Name: COLUMN sys_schedule_log.job_code; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_log.job_code IS '任务编码';


--
-- TOC entry 4461 (class 0 OID 0)
-- Dependencies: 273
-- Name: COLUMN sys_schedule_log.params; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_log.params IS '参数';


--
-- TOC entry 4462 (class 0 OID 0)
-- Dependencies: 273
-- Name: COLUMN sys_schedule_log.execute_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_schedule_log.execute_time IS '执行时间(毫秒)';


--
-- TOC entry 272 (class 1259 OID 34755)
-- Name: sys_schedule_log_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sys_schedule_log_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_schedule_log_id_seq OWNER TO postgres;

--
-- TOC entry 4463 (class 0 OID 0)
-- Dependencies: 272
-- Name: sys_schedule_log_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sys_schedule_log_id_seq OWNED BY public.sys_schedule_log.id;


--
-- TOC entry 263 (class 1259 OID 34573)
-- Name: sys_user_role; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.sys_user_role (
    id bigint NOT NULL,
    employee_id bigint NOT NULL,
    role_id bigint NOT NULL,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);


ALTER TABLE public.sys_user_role OWNER TO postgres;

--
-- TOC entry 4464 (class 0 OID 0)
-- Dependencies: 263
-- Name: TABLE sys_user_role; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON TABLE public.sys_user_role IS '用户角色关联表';


--
-- TOC entry 4465 (class 0 OID 0)
-- Dependencies: 263
-- Name: COLUMN sys_user_role.id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_user_role.id IS 'ID';


--
-- TOC entry 4466 (class 0 OID 0)
-- Dependencies: 263
-- Name: COLUMN sys_user_role.employee_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_user_role.employee_id IS '人员ID';


--
-- TOC entry 4467 (class 0 OID 0)
-- Dependencies: 263
-- Name: COLUMN sys_user_role.role_id; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_user_role.role_id IS '角色ID';


--
-- TOC entry 4468 (class 0 OID 0)
-- Dependencies: 263
-- Name: COLUMN sys_user_role.create_time; Type: COMMENT; Schema: public; Owner: postgres
--

COMMENT ON COLUMN public.sys_user_role.create_time IS '创建时间';


--
-- TOC entry 262 (class 1259 OID 34572)
-- Name: sys_user_role_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.sys_user_role_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.sys_user_role_id_seq OWNER TO postgres;

--
-- TOC entry 4469 (class 0 OID 0)
-- Dependencies: 262
-- Name: sys_user_role_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.sys_user_role_id_seq OWNED BY public.sys_user_role.id;


--
-- TOC entry 3609 (class 2604 OID 34598)
-- Name: approval_record id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.approval_record ALTER COLUMN id SET DEFAULT nextval('public.approval_record_id_seq'::regclass);


--
-- TOC entry 3575 (class 2604 OID 34394)
-- Name: duty_assignment id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_assignment ALTER COLUMN id SET DEFAULT nextval('public.duty_assignment_id_seq'::regclass);


--
-- TOC entry 3516 (class 2604 OID 34144)
-- Name: duty_holiday id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_holiday ALTER COLUMN id SET DEFAULT nextval('public.duty_holiday_id_seq'::regclass);


--
-- TOC entry 3613 (class 2604 OID 34628)
-- Name: duty_record id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_record ALTER COLUMN id SET DEFAULT nextval('public.duty_record_id_seq'::regclass);


--
-- TOC entry 3550 (class 2604 OID 34256)
-- Name: duty_schedule id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule ALTER COLUMN id SET DEFAULT nextval('public.duty_schedule_id_seq'::regclass);


--
-- TOC entry 3564 (class 2604 OID 34323)
-- Name: duty_schedule_employee id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule_employee ALTER COLUMN id SET DEFAULT nextval('public.duty_schedule_employee_id_seq'::regclass);


--
-- TOC entry 3487 (class 2604 OID 34071)
-- Name: duty_schedule_mode id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule_mode ALTER COLUMN id SET DEFAULT nextval('public.duty_schedule_mode_id_seq'::regclass);


--
-- TOC entry 3494 (class 2604 OID 34093)
-- Name: duty_schedule_rule id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule_rule ALTER COLUMN id SET DEFAULT nextval('public.duty_schedule_rule_id_seq'::regclass);


--
-- TOC entry 3568 (class 2604 OID 34349)
-- Name: duty_schedule_shift id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule_shift ALTER COLUMN id SET DEFAULT nextval('public.duty_schedule_shift_id_seq'::regclass);


--
-- TOC entry 3505 (class 2604 OID 34118)
-- Name: duty_shift_config id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_shift_config ALTER COLUMN id SET DEFAULT nextval('public.duty_shift_config_id_seq'::regclass);


--
-- TOC entry 3556 (class 2604 OID 34277)
-- Name: duty_shift_mutex id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_shift_mutex ALTER COLUMN id SET DEFAULT nextval('public.duty_shift_mutex_id_seq'::regclass);


--
-- TOC entry 3559 (class 2604 OID 34302)
-- Name: employee_available_time id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employee_available_time ALTER COLUMN id SET DEFAULT nextval('public.employee_available_time_id_seq'::regclass);


--
-- TOC entry 3582 (class 2604 OID 34434)
-- Name: leave_request id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.leave_request ALTER COLUMN id SET DEFAULT nextval('public.leave_request_id_seq'::regclass);


--
-- TOC entry 3623 (class 2604 OID 34665)
-- Name: leave_substitute id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.leave_substitute ALTER COLUMN id SET DEFAULT nextval('public.leave_substitute_id_seq'::regclass);


--
-- TOC entry 3596 (class 2604 OID 34493)
-- Name: operation_log id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.operation_log ALTER COLUMN id SET DEFAULT nextval('public.operation_log_id_seq'::regclass);


--
-- TOC entry 3639 (class 2604 OID 34777)
-- Name: pm_project id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_project ALTER COLUMN id SET DEFAULT nextval('public.pm_project_id_seq'::regclass);


--
-- TOC entry 3657 (class 2604 OID 57349)
-- Name: pm_project_employee id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_project_employee ALTER COLUMN id SET DEFAULT nextval('public.pm_project_employee_id_seq'::regclass);


--
-- TOC entry 3647 (class 2604 OID 34797)
-- Name: pm_task id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task ALTER COLUMN id SET DEFAULT nextval('public.pm_task_id_seq'::regclass);


--
-- TOC entry 3664 (class 2604 OID 57399)
-- Name: pm_task_comment id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task_comment ALTER COLUMN id SET DEFAULT nextval('public.pm_task_comment_id_seq'::regclass);


--
-- TOC entry 3667 (class 2604 OID 57423)
-- Name: pm_task_comment_attachment id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task_comment_attachment ALTER COLUMN id SET DEFAULT nextval('public.pm_task_comment_attachment_id_seq'::regclass);


--
-- TOC entry 3660 (class 2604 OID 57373)
-- Name: pm_task_stakeholder id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task_stakeholder ALTER COLUMN id SET DEFAULT nextval('public.pm_task_stakeholder_id_seq'::regclass);


--
-- TOC entry 3571 (class 2604 OID 34372)
-- Name: schedule_version id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.schedule_version ALTER COLUMN id SET DEFAULT nextval('public.schedule_version_id_seq'::regclass);


--
-- TOC entry 3627 (class 2604 OID 34703)
-- Name: swap_request id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.swap_request ALTER COLUMN id SET DEFAULT nextval('public.swap_request_id_seq'::regclass);


--
-- TOC entry 3460 (class 2604 OID 34000)
-- Name: sys_dept id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_dept ALTER COLUMN id SET DEFAULT nextval('public.sys_dept_id_seq'::regclass);


--
-- TOC entry 3541 (class 2604 OID 34229)
-- Name: sys_dict_data id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_dict_data ALTER COLUMN id SET DEFAULT nextval('public.sys_dict_data_id_seq'::regclass);


--
-- TOC entry 3466 (class 2604 OID 34016)
-- Name: sys_dict_type id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_dict_type ALTER COLUMN id SET DEFAULT nextval('public.sys_dict_type_id_seq'::regclass);


--
-- TOC entry 3530 (class 2604 OID 34201)
-- Name: sys_employee id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_employee ALTER COLUMN id SET DEFAULT nextval('public.sys_employee_id_seq'::regclass);


--
-- TOC entry 3471 (class 2604 OID 34033)
-- Name: sys_menu id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_menu ALTER COLUMN id SET DEFAULT nextval('public.sys_menu_id_seq'::regclass);


--
-- TOC entry 3481 (class 2604 OID 34056)
-- Name: sys_role id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_role ALTER COLUMN id SET DEFAULT nextval('public.sys_role_id_seq'::regclass);


--
-- TOC entry 3605 (class 2604 OID 34552)
-- Name: sys_role_menu id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_role_menu ALTER COLUMN id SET DEFAULT nextval('public.sys_role_menu_id_seq'::regclass);


--
-- TOC entry 3522 (class 2604 OID 34177)
-- Name: sys_schedule_job id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_schedule_job ALTER COLUMN id SET DEFAULT nextval('public.sys_schedule_job_id_seq'::regclass);


--
-- TOC entry 3637 (class 2604 OID 34759)
-- Name: sys_schedule_log id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_schedule_log ALTER COLUMN id SET DEFAULT nextval('public.sys_schedule_log_id_seq'::regclass);


--
-- TOC entry 3607 (class 2604 OID 34576)
-- Name: sys_user_role id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_user_role ALTER COLUMN id SET DEFAULT nextval('public.sys_user_role_id_seq'::regclass);


--
-- TOC entry 3768 (class 2606 OID 34611)
-- Name: approval_record approval_record_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.approval_record
    ADD CONSTRAINT approval_record_pkey PRIMARY KEY (id);


--
-- TOC entry 3742 (class 2606 OID 34406)
-- Name: duty_assignment duty_assignment_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_assignment
    ADD CONSTRAINT duty_assignment_pkey PRIMARY KEY (id);


--
-- TOC entry 3699 (class 2606 OID 34154)
-- Name: duty_holiday duty_holiday_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_holiday
    ADD CONSTRAINT duty_holiday_pkey PRIMARY KEY (id);


--
-- TOC entry 3773 (class 2606 OID 34644)
-- Name: duty_record duty_record_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_record
    ADD CONSTRAINT duty_record_pkey PRIMARY KEY (id);


--
-- TOC entry 3731 (class 2606 OID 34331)
-- Name: duty_schedule_employee duty_schedule_employee_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule_employee
    ADD CONSTRAINT duty_schedule_employee_pkey PRIMARY KEY (id);


--
-- TOC entry 3686 (class 2606 OID 34085)
-- Name: duty_schedule_mode duty_schedule_mode_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule_mode
    ADD CONSTRAINT duty_schedule_mode_pkey PRIMARY KEY (id);


--
-- TOC entry 3719 (class 2606 OID 34266)
-- Name: duty_schedule duty_schedule_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule
    ADD CONSTRAINT duty_schedule_pkey PRIMARY KEY (id);


--
-- TOC entry 3691 (class 2606 OID 34111)
-- Name: duty_schedule_rule duty_schedule_rule_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule_rule
    ADD CONSTRAINT duty_schedule_rule_pkey PRIMARY KEY (id);


--
-- TOC entry 3736 (class 2606 OID 34356)
-- Name: duty_schedule_shift duty_schedule_shift_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule_shift
    ADD CONSTRAINT duty_schedule_shift_pkey PRIMARY KEY (id);


--
-- TOC entry 3695 (class 2606 OID 34137)
-- Name: duty_shift_config duty_shift_config_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_shift_config
    ADD CONSTRAINT duty_shift_config_pkey PRIMARY KEY (id);


--
-- TOC entry 3722 (class 2606 OID 34284)
-- Name: duty_shift_mutex duty_shift_mutex_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_shift_mutex
    ADD CONSTRAINT duty_shift_mutex_pkey PRIMARY KEY (id);


--
-- TOC entry 3727 (class 2606 OID 34311)
-- Name: employee_available_time employee_available_time_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employee_available_time
    ADD CONSTRAINT employee_available_time_pkey PRIMARY KEY (id);


--
-- TOC entry 3751 (class 2606 OID 34458)
-- Name: leave_request leave_request_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.leave_request
    ADD CONSTRAINT leave_request_pkey PRIMARY KEY (id);


--
-- TOC entry 3777 (class 2606 OID 34676)
-- Name: leave_substitute leave_substitute_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.leave_substitute
    ADD CONSTRAINT leave_substitute_pkey PRIMARY KEY (id);


--
-- TOC entry 3758 (class 2606 OID 34508)
-- Name: operation_log operation_log_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.operation_log
    ADD CONSTRAINT operation_log_pkey PRIMARY KEY (id);


--
-- TOC entry 3802 (class 2606 OID 57356)
-- Name: pm_project_employee pm_project_employee_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_project_employee
    ADD CONSTRAINT pm_project_employee_pkey PRIMARY KEY (id);


--
-- TOC entry 3804 (class 2606 OID 57358)
-- Name: pm_project_employee pm_project_employee_project_id_employee_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_project_employee
    ADD CONSTRAINT pm_project_employee_project_id_employee_id_key UNIQUE (project_id, employee_id);


--
-- TOC entry 3791 (class 2606 OID 34789)
-- Name: pm_project pm_project_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_project
    ADD CONSTRAINT pm_project_pkey PRIMARY KEY (id);


--
-- TOC entry 3812 (class 2606 OID 57433)
-- Name: pm_task_comment_attachment pm_task_comment_attachment_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task_comment_attachment
    ADD CONSTRAINT pm_task_comment_attachment_pkey PRIMARY KEY (id);


--
-- TOC entry 3810 (class 2606 OID 57408)
-- Name: pm_task_comment pm_task_comment_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task_comment
    ADD CONSTRAINT pm_task_comment_pkey PRIMARY KEY (id);


--
-- TOC entry 3799 (class 2606 OID 34808)
-- Name: pm_task pm_task_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task
    ADD CONSTRAINT pm_task_pkey PRIMARY KEY (id);


--
-- TOC entry 3806 (class 2606 OID 57382)
-- Name: pm_task_stakeholder pm_task_stakeholder_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task_stakeholder
    ADD CONSTRAINT pm_task_stakeholder_pkey PRIMARY KEY (id);


--
-- TOC entry 3808 (class 2606 OID 57384)
-- Name: pm_task_stakeholder pm_task_stakeholder_task_id_employee_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task_stakeholder
    ADD CONSTRAINT pm_task_stakeholder_task_id_employee_id_key UNIQUE (task_id, employee_id);


--
-- TOC entry 3740 (class 2606 OID 34383)
-- Name: schedule_version schedule_version_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.schedule_version
    ADD CONSTRAINT schedule_version_pkey PRIMARY KEY (id);


--
-- TOC entry 3784 (class 2606 OID 34724)
-- Name: swap_request swap_request_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.swap_request
    ADD CONSTRAINT swap_request_pkey PRIMARY KEY (id);


--
-- TOC entry 3670 (class 2606 OID 34010)
-- Name: sys_dept sys_dept_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_dept
    ADD CONSTRAINT sys_dept_pkey PRIMARY KEY (id);


--
-- TOC entry 3717 (class 2606 OID 34245)
-- Name: sys_dict_data sys_dict_data_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_dict_data
    ADD CONSTRAINT sys_dict_data_pkey PRIMARY KEY (id);


--
-- TOC entry 3675 (class 2606 OID 34025)
-- Name: sys_dict_type sys_dict_type_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_dict_type
    ADD CONSTRAINT sys_dict_type_pkey PRIMARY KEY (id);


--
-- TOC entry 3713 (class 2606 OID 34215)
-- Name: sys_employee sys_employee_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_employee
    ADD CONSTRAINT sys_employee_pkey PRIMARY KEY (id);


--
-- TOC entry 3680 (class 2606 OID 34049)
-- Name: sys_menu sys_menu_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_menu
    ADD CONSTRAINT sys_menu_pkey PRIMARY KEY (id);


--
-- TOC entry 3762 (class 2606 OID 34558)
-- Name: sys_role_menu sys_role_menu_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_role_menu
    ADD CONSTRAINT sys_role_menu_pkey PRIMARY KEY (id);


--
-- TOC entry 3683 (class 2606 OID 34065)
-- Name: sys_role sys_role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_role
    ADD CONSTRAINT sys_role_pkey PRIMARY KEY (id);


--
-- TOC entry 3707 (class 2606 OID 34193)
-- Name: sys_schedule_job sys_schedule_job_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_schedule_job
    ADD CONSTRAINT sys_schedule_job_pkey PRIMARY KEY (id);


--
-- TOC entry 3787 (class 2606 OID 34770)
-- Name: sys_schedule_log sys_schedule_log_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_schedule_log
    ADD CONSTRAINT sys_schedule_log_pkey PRIMARY KEY (id);


--
-- TOC entry 3765 (class 2606 OID 34582)
-- Name: sys_user_role sys_user_role_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT sys_user_role_pkey PRIMARY KEY (id);


--
-- TOC entry 3771 (class 1259 OID 34660)
-- Name: assignment_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX assignment_id ON public.duty_record USING btree (assignment_id);


--
-- TOC entry 3743 (class 1259 OID 34428)
-- Name: employee_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX employee_id ON public.duty_assignment USING btree (employee_id);


--
-- TOC entry 3720 (class 1259 OID 34272)
-- Name: fk_schedule_mode; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX fk_schedule_mode ON public.duty_schedule USING btree (schedule_mode_id);


--
-- TOC entry 3746 (class 1259 OID 34485)
-- Name: idx_approval_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_approval_status ON public.leave_request USING btree (approval_status);


--
-- TOC entry 3769 (class 1259 OID 34623)
-- Name: idx_approver_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_approver_id ON public.approval_record USING btree (approver_id);


--
-- TOC entry 3788 (class 1259 OID 34792)
-- Name: idx_archived; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_archived ON public.pm_project USING btree (archived);


--
-- TOC entry 3793 (class 1259 OID 34822)
-- Name: idx_assignee_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_assignee_id ON public.pm_task USING btree (assignee_id);


--
-- TOC entry 3753 (class 1259 OID 34517)
-- Name: idx_create_time; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_create_time ON public.operation_log USING btree (create_time);


--
-- TOC entry 3747 (class 1259 OID 34487)
-- Name: idx_current_approver_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_current_approver_id ON public.leave_request USING btree (current_approver_id);


--
-- TOC entry 3748 (class 1259 OID 34486)
-- Name: idx_date_range; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_date_range ON public.leave_request USING btree (start_date, end_date);


--
-- TOC entry 3672 (class 1259 OID 34027)
-- Name: idx_dict_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dict_code ON public.sys_dict_type USING btree (dict_code);


--
-- TOC entry 3708 (class 1259 OID 34224)
-- Name: idx_dict_data_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dict_data_id ON public.sys_employee USING btree (dict_data_id);


--
-- TOC entry 3709 (class 1259 OID 34223)
-- Name: idx_dict_type_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dict_type_id ON public.sys_employee USING btree (dict_type_id);


--
-- TOC entry 3715 (class 1259 OID 34251)
-- Name: idx_dict_value; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_dict_value ON public.sys_dict_data USING btree (dict_value);


--
-- TOC entry 3774 (class 1259 OID 34698)
-- Name: idx_duty_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_duty_date ON public.leave_substitute USING btree (duty_date);


--
-- TOC entry 3710 (class 1259 OID 34222)
-- Name: idx_employee_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_employee_code ON public.sys_employee USING btree (employee_code);


--
-- TOC entry 3728 (class 1259 OID 34318)
-- Name: idx_employee_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_employee_id ON public.employee_available_time USING btree (employee_id);


--
-- TOC entry 3711 (class 1259 OID 34828)
-- Name: idx_employee_sort; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_employee_sort ON public.sys_employee USING btree (sort);


--
-- TOC entry 3700 (class 1259 OID 34156)
-- Name: idx_holiday_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_holiday_date ON public.duty_holiday USING btree (holiday_date);


--
-- TOC entry 3732 (class 1259 OID 34344)
-- Name: idx_is_leader; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_is_leader ON public.duty_schedule_employee USING btree (is_leader);


--
-- TOC entry 3794 (class 1259 OID 49158)
-- Name: idx_is_pinned; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_is_pinned ON public.pm_task USING btree (is_pinned);


--
-- TOC entry 3701 (class 1259 OID 34157)
-- Name: idx_is_workday; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_is_workday ON public.duty_holiday USING btree (is_workday);


--
-- TOC entry 3703 (class 1259 OID 34195)
-- Name: idx_job_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_job_code ON public.sys_schedule_job USING btree (job_code);


--
-- TOC entry 3704 (class 1259 OID 34196)
-- Name: idx_job_group; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_job_group ON public.sys_schedule_job USING btree (job_group);


--
-- TOC entry 3785 (class 1259 OID 40965)
-- Name: idx_job_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_job_id ON public.sys_schedule_log USING btree (job_id);


--
-- TOC entry 3775 (class 1259 OID 34697)
-- Name: idx_leave_request_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_leave_request_id ON public.leave_substitute USING btree (leave_request_id);


--
-- TOC entry 3759 (class 1259 OID 34571)
-- Name: idx_menu_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_menu_id ON public.sys_role_menu USING btree (menu_id);


--
-- TOC entry 3687 (class 1259 OID 34087)
-- Name: idx_mode_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_mode_code ON public.duty_schedule_mode USING btree (mode_code);


--
-- TOC entry 3688 (class 1259 OID 34088)
-- Name: idx_mode_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_mode_type ON public.duty_schedule_mode USING btree (mode_type);


--
-- TOC entry 3723 (class 1259 OID 34297)
-- Name: idx_mutex_shift_config_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_mutex_shift_config_id ON public.duty_shift_mutex USING btree (mutex_shift_config_id);


--
-- TOC entry 3754 (class 1259 OID 34516)
-- Name: idx_operation_module; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_operation_module ON public.operation_log USING btree (operation_module);


--
-- TOC entry 3755 (class 1259 OID 34515)
-- Name: idx_operation_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_operation_type ON public.operation_log USING btree (operation_type);


--
-- TOC entry 3756 (class 1259 OID 34514)
-- Name: idx_operator_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_operator_id ON public.operation_log USING btree (operator_id);


--
-- TOC entry 3778 (class 1259 OID 34753)
-- Name: idx_original_assignment_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_original_assignment_id ON public.swap_request USING btree (original_assignment_id);


--
-- TOC entry 3779 (class 1259 OID 34750)
-- Name: idx_original_employee; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_original_employee ON public.swap_request USING btree (original_employee_id);


--
-- TOC entry 3789 (class 1259 OID 34791)
-- Name: idx_owner_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_owner_id ON public.pm_project USING btree (owner_id);


--
-- TOC entry 3677 (class 1259 OID 34050)
-- Name: idx_parent_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_parent_id ON public.sys_menu USING btree (parent_id);


--
-- TOC entry 3795 (class 1259 OID 34821)
-- Name: idx_priority; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_priority ON public.pm_task USING btree (priority);


--
-- TOC entry 3796 (class 1259 OID 34820)
-- Name: idx_project_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_project_id ON public.pm_task USING btree (project_id);


--
-- TOC entry 3770 (class 1259 OID 34622)
-- Name: idx_request_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_request_id ON public.approval_record USING btree (request_id);


--
-- TOC entry 3760 (class 1259 OID 34570)
-- Name: idx_role_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_role_id ON public.sys_role_menu USING btree (role_id);


--
-- TOC entry 3681 (class 1259 OID 40962)
-- Name: idx_role_sort; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_role_sort ON public.sys_role USING btree (sort);


--
-- TOC entry 3692 (class 1259 OID 34113)
-- Name: idx_rule_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_rule_code ON public.duty_schedule_rule USING btree (rule_code);


--
-- TOC entry 3733 (class 1259 OID 34343)
-- Name: idx_schedule_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_schedule_id ON public.duty_schedule_employee USING btree (schedule_id);


--
-- TOC entry 3696 (class 1259 OID 34139)
-- Name: idx_shift_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_shift_code ON public.duty_shift_config USING btree (shift_code);


--
-- TOC entry 3724 (class 1259 OID 34296)
-- Name: idx_shift_config_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_shift_config_id ON public.duty_shift_mutex USING btree (shift_config_id);


--
-- TOC entry 3673 (class 1259 OID 34028)
-- Name: idx_status; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_status ON public.sys_dict_type USING btree (status);


--
-- TOC entry 3749 (class 1259 OID 34488)
-- Name: idx_substitute_employee_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_substitute_employee_id ON public.leave_request USING btree (substitute_employee_id);


--
-- TOC entry 3780 (class 1259 OID 34752)
-- Name: idx_swap_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_swap_date ON public.swap_request USING btree (swap_date);


--
-- TOC entry 3781 (class 1259 OID 34754)
-- Name: idx_target_assignment_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_target_assignment_id ON public.swap_request USING btree (target_assignment_id);


--
-- TOC entry 3782 (class 1259 OID 34751)
-- Name: idx_target_employee; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_target_employee ON public.swap_request USING btree (target_employee_id);


--
-- TOC entry 3797 (class 1259 OID 49157)
-- Name: idx_task_level; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_task_level ON public.pm_task USING btree (task_level);


--
-- TOC entry 3678 (class 1259 OID 34051)
-- Name: idx_type; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_type ON public.sys_menu USING btree (type);


--
-- TOC entry 3738 (class 1259 OID 34389)
-- Name: idx_version_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_version_code ON public.schedule_version USING btree (version_code);


--
-- TOC entry 3744 (class 1259 OID 34429)
-- Name: idx_version_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX idx_version_id ON public.duty_assignment USING btree (version_id);


--
-- TOC entry 3705 (class 1259 OID 34194)
-- Name: job_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX job_code ON public.sys_schedule_job USING btree (job_code);


--
-- TOC entry 3689 (class 1259 OID 34086)
-- Name: mode_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX mode_code ON public.duty_schedule_mode USING btree (mode_code);


--
-- TOC entry 3693 (class 1259 OID 34112)
-- Name: rule_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX rule_code ON public.duty_schedule_rule USING btree (rule_code);


--
-- TOC entry 3745 (class 1259 OID 34427)
-- Name: schedule_id; Type: INDEX; Schema: public; Owner: postgres
--

CREATE INDEX schedule_id ON public.duty_assignment USING btree (schedule_id);


--
-- TOC entry 3697 (class 1259 OID 34138)
-- Name: shift_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX shift_code ON public.duty_shift_config USING btree (shift_code);


--
-- TOC entry 3671 (class 1259 OID 34011)
-- Name: uk_dept_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uk_dept_code ON public.sys_dept USING btree (dept_code);


--
-- TOC entry 3676 (class 1259 OID 34026)
-- Name: uk_dict_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uk_dict_code ON public.sys_dict_type USING btree (dict_code);


--
-- TOC entry 3714 (class 1259 OID 34221)
-- Name: uk_employee_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uk_employee_code ON public.sys_employee USING btree (employee_code);


--
-- TOC entry 3729 (class 1259 OID 34317)
-- Name: uk_employee_day; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uk_employee_day ON public.employee_available_time USING btree (employee_id, day_of_week);


--
-- TOC entry 3766 (class 1259 OID 34593)
-- Name: uk_employee_role; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uk_employee_role ON public.sys_user_role USING btree (employee_id, role_id);


--
-- TOC entry 3702 (class 1259 OID 34155)
-- Name: uk_holiday_date; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uk_holiday_date ON public.duty_holiday USING btree (holiday_date);


--
-- TOC entry 3792 (class 1259 OID 34790)
-- Name: uk_project_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uk_project_code ON public.pm_project USING btree (project_code);


--
-- TOC entry 3752 (class 1259 OID 34484)
-- Name: uk_request_no; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uk_request_no ON public.leave_request USING btree (request_no);


--
-- TOC entry 3684 (class 1259 OID 34066)
-- Name: uk_role_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uk_role_code ON public.sys_role USING btree (role_code);


--
-- TOC entry 3763 (class 1259 OID 34569)
-- Name: uk_role_menu; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uk_role_menu ON public.sys_role_menu USING btree (role_id, menu_id);


--
-- TOC entry 3734 (class 1259 OID 34342)
-- Name: uk_schedule_employee; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uk_schedule_employee ON public.duty_schedule_employee USING btree (schedule_id, employee_id);


--
-- TOC entry 3737 (class 1259 OID 34367)
-- Name: uk_schedule_shift; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uk_schedule_shift ON public.duty_schedule_shift USING btree (schedule_id, shift_config_id);


--
-- TOC entry 3725 (class 1259 OID 34295)
-- Name: uk_shift_mutex; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uk_shift_mutex ON public.duty_shift_mutex USING btree (shift_config_id, mutex_shift_config_id);


--
-- TOC entry 3800 (class 1259 OID 34819)
-- Name: uk_task_code; Type: INDEX; Schema: public; Owner: postgres
--

CREATE UNIQUE INDEX uk_task_code ON public.pm_task USING btree (task_code);


--
-- TOC entry 3838 (class 2606 OID 34612)
-- Name: approval_record approval_record_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.approval_record
    ADD CONSTRAINT approval_record_ibfk_1 FOREIGN KEY (request_id) REFERENCES public.leave_request(id) ON DELETE CASCADE;


--
-- TOC entry 3839 (class 2606 OID 34617)
-- Name: approval_record approval_record_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.approval_record
    ADD CONSTRAINT approval_record_ibfk_2 FOREIGN KEY (approver_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;


--
-- TOC entry 3824 (class 2606 OID 34407)
-- Name: duty_assignment duty_assignment_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_assignment
    ADD CONSTRAINT duty_assignment_ibfk_1 FOREIGN KEY (schedule_id) REFERENCES public.duty_schedule(id) ON DELETE CASCADE;


--
-- TOC entry 3825 (class 2606 OID 34412)
-- Name: duty_assignment duty_assignment_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_assignment
    ADD CONSTRAINT duty_assignment_ibfk_2 FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;


--
-- TOC entry 3840 (class 2606 OID 34645)
-- Name: duty_record duty_record_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_record
    ADD CONSTRAINT duty_record_ibfk_1 FOREIGN KEY (assignment_id) REFERENCES public.duty_assignment(id) ON DELETE CASCADE;


--
-- TOC entry 3841 (class 2606 OID 34650)
-- Name: duty_record duty_record_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_record
    ADD CONSTRAINT duty_record_ibfk_2 FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;


--
-- TOC entry 3819 (class 2606 OID 34332)
-- Name: duty_schedule_employee duty_schedule_employee_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule_employee
    ADD CONSTRAINT duty_schedule_employee_ibfk_1 FOREIGN KEY (schedule_id) REFERENCES public.duty_schedule(id) ON DELETE CASCADE;


--
-- TOC entry 3820 (class 2606 OID 34337)
-- Name: duty_schedule_employee duty_schedule_employee_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule_employee
    ADD CONSTRAINT duty_schedule_employee_ibfk_2 FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;


--
-- TOC entry 3821 (class 2606 OID 34357)
-- Name: duty_schedule_shift duty_schedule_shift_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule_shift
    ADD CONSTRAINT duty_schedule_shift_ibfk_1 FOREIGN KEY (schedule_id) REFERENCES public.duty_schedule(id) ON DELETE CASCADE;


--
-- TOC entry 3822 (class 2606 OID 34362)
-- Name: duty_schedule_shift duty_schedule_shift_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule_shift
    ADD CONSTRAINT duty_schedule_shift_ibfk_2 FOREIGN KEY (shift_config_id) REFERENCES public.duty_shift_config(id) ON DELETE CASCADE;


--
-- TOC entry 3816 (class 2606 OID 34285)
-- Name: duty_shift_mutex duty_shift_mutex_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_shift_mutex
    ADD CONSTRAINT duty_shift_mutex_ibfk_1 FOREIGN KEY (shift_config_id) REFERENCES public.duty_shift_config(id) ON DELETE CASCADE;


--
-- TOC entry 3817 (class 2606 OID 34290)
-- Name: duty_shift_mutex duty_shift_mutex_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_shift_mutex
    ADD CONSTRAINT duty_shift_mutex_ibfk_2 FOREIGN KEY (mutex_shift_config_id) REFERENCES public.duty_shift_config(id) ON DELETE CASCADE;


--
-- TOC entry 3818 (class 2606 OID 34312)
-- Name: employee_available_time employee_available_time_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.employee_available_time
    ADD CONSTRAINT employee_available_time_ibfk_1 FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;


--
-- TOC entry 3828 (class 2606 OID 34459)
-- Name: leave_request fk_schedule; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.leave_request
    ADD CONSTRAINT fk_schedule FOREIGN KEY (schedule_id) REFERENCES public.duty_schedule(id) ON DELETE SET NULL;


--
-- TOC entry 3815 (class 2606 OID 34267)
-- Name: duty_schedule fk_schedule_mode; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_schedule
    ADD CONSTRAINT fk_schedule_mode FOREIGN KEY (schedule_mode_id) REFERENCES public.duty_schedule_mode(id) ON DELETE SET NULL;


--
-- TOC entry 3826 (class 2606 OID 34417)
-- Name: duty_assignment fk_shift_config; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_assignment
    ADD CONSTRAINT fk_shift_config FOREIGN KEY (shift_config_id) REFERENCES public.duty_shift_config(id) ON DELETE SET NULL;


--
-- TOC entry 3842 (class 2606 OID 34655)
-- Name: duty_record fk_substitute_employee; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_record
    ADD CONSTRAINT fk_substitute_employee FOREIGN KEY (substitute_employee_id) REFERENCES public.sys_employee(id) ON DELETE SET NULL;


--
-- TOC entry 3827 (class 2606 OID 34422)
-- Name: duty_assignment fk_version; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.duty_assignment
    ADD CONSTRAINT fk_version FOREIGN KEY (version_id) REFERENCES public.schedule_version(id) ON DELETE SET NULL;


--
-- TOC entry 3829 (class 2606 OID 34464)
-- Name: leave_request leave_request_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.leave_request
    ADD CONSTRAINT leave_request_ibfk_1 FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;


--
-- TOC entry 3830 (class 2606 OID 34469)
-- Name: leave_request leave_request_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.leave_request
    ADD CONSTRAINT leave_request_ibfk_2 FOREIGN KEY (current_approver_id) REFERENCES public.sys_employee(id) ON DELETE SET NULL;


--
-- TOC entry 3831 (class 2606 OID 34474)
-- Name: leave_request leave_request_ibfk_3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.leave_request
    ADD CONSTRAINT leave_request_ibfk_3 FOREIGN KEY (substitute_employee_id) REFERENCES public.sys_employee(id) ON DELETE SET NULL;


--
-- TOC entry 3832 (class 2606 OID 34479)
-- Name: leave_request leave_request_ibfk_4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.leave_request
    ADD CONSTRAINT leave_request_ibfk_4 FOREIGN KEY (shift_config_id) REFERENCES public.duty_shift_config(id) ON DELETE SET NULL;


--
-- TOC entry 3843 (class 2606 OID 34677)
-- Name: leave_substitute leave_substitute_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.leave_substitute
    ADD CONSTRAINT leave_substitute_ibfk_1 FOREIGN KEY (leave_request_id) REFERENCES public.leave_request(id) ON DELETE CASCADE;


--
-- TOC entry 3844 (class 2606 OID 34682)
-- Name: leave_substitute leave_substitute_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.leave_substitute
    ADD CONSTRAINT leave_substitute_ibfk_2 FOREIGN KEY (original_employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;


--
-- TOC entry 3845 (class 2606 OID 34687)
-- Name: leave_substitute leave_substitute_ibfk_3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.leave_substitute
    ADD CONSTRAINT leave_substitute_ibfk_3 FOREIGN KEY (substitute_employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;


--
-- TOC entry 3846 (class 2606 OID 34692)
-- Name: leave_substitute leave_substitute_ibfk_4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.leave_substitute
    ADD CONSTRAINT leave_substitute_ibfk_4 FOREIGN KEY (shift_config_id) REFERENCES public.duty_shift_config(id) ON DELETE CASCADE;


--
-- TOC entry 3833 (class 2606 OID 34509)
-- Name: operation_log operation_log_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.operation_log
    ADD CONSTRAINT operation_log_ibfk_1 FOREIGN KEY (operator_id) REFERENCES public.sys_employee(id) ON DELETE SET NULL;


--
-- TOC entry 3854 (class 2606 OID 57364)
-- Name: pm_project_employee pm_project_employee_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_project_employee
    ADD CONSTRAINT pm_project_employee_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;


--
-- TOC entry 3855 (class 2606 OID 57359)
-- Name: pm_project_employee pm_project_employee_project_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_project_employee
    ADD CONSTRAINT pm_project_employee_project_id_fkey FOREIGN KEY (project_id) REFERENCES public.pm_project(id) ON DELETE CASCADE;


--
-- TOC entry 3860 (class 2606 OID 57434)
-- Name: pm_task_comment_attachment pm_task_comment_attachment_comment_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task_comment_attachment
    ADD CONSTRAINT pm_task_comment_attachment_comment_id_fkey FOREIGN KEY (comment_id) REFERENCES public.pm_task_comment(id) ON DELETE CASCADE;


--
-- TOC entry 3858 (class 2606 OID 57414)
-- Name: pm_task_comment pm_task_comment_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task_comment
    ADD CONSTRAINT pm_task_comment_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;


--
-- TOC entry 3859 (class 2606 OID 57409)
-- Name: pm_task_comment pm_task_comment_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task_comment
    ADD CONSTRAINT pm_task_comment_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.pm_task(id) ON DELETE CASCADE;


--
-- TOC entry 3852 (class 2606 OID 34809)
-- Name: pm_task pm_task_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task
    ADD CONSTRAINT pm_task_ibfk_1 FOREIGN KEY (project_id) REFERENCES public.pm_project(id) ON DELETE CASCADE;


--
-- TOC entry 3853 (class 2606 OID 34814)
-- Name: pm_task pm_task_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task
    ADD CONSTRAINT pm_task_ibfk_2 FOREIGN KEY (assignee_id) REFERENCES public.sys_employee(id) ON DELETE SET NULL;


--
-- TOC entry 3856 (class 2606 OID 57390)
-- Name: pm_task_stakeholder pm_task_stakeholder_employee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task_stakeholder
    ADD CONSTRAINT pm_task_stakeholder_employee_id_fkey FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;


--
-- TOC entry 3857 (class 2606 OID 57385)
-- Name: pm_task_stakeholder pm_task_stakeholder_task_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.pm_task_stakeholder
    ADD CONSTRAINT pm_task_stakeholder_task_id_fkey FOREIGN KEY (task_id) REFERENCES public.pm_task(id) ON DELETE CASCADE;


--
-- TOC entry 3823 (class 2606 OID 34384)
-- Name: schedule_version schedule_version_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.schedule_version
    ADD CONSTRAINT schedule_version_ibfk_1 FOREIGN KEY (schedule_id) REFERENCES public.duty_schedule(id) ON DELETE CASCADE;


--
-- TOC entry 3847 (class 2606 OID 34725)
-- Name: swap_request swap_request_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.swap_request
    ADD CONSTRAINT swap_request_ibfk_1 FOREIGN KEY (original_employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;


--
-- TOC entry 3848 (class 2606 OID 34730)
-- Name: swap_request swap_request_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.swap_request
    ADD CONSTRAINT swap_request_ibfk_2 FOREIGN KEY (target_employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;


--
-- TOC entry 3849 (class 2606 OID 34735)
-- Name: swap_request swap_request_ibfk_3; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.swap_request
    ADD CONSTRAINT swap_request_ibfk_3 FOREIGN KEY (original_assignment_id) REFERENCES public.duty_assignment(id) ON DELETE CASCADE;


--
-- TOC entry 3850 (class 2606 OID 34740)
-- Name: swap_request swap_request_ibfk_4; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.swap_request
    ADD CONSTRAINT swap_request_ibfk_4 FOREIGN KEY (target_assignment_id) REFERENCES public.duty_assignment(id) ON DELETE CASCADE;


--
-- TOC entry 3851 (class 2606 OID 34745)
-- Name: swap_request swap_request_ibfk_5; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.swap_request
    ADD CONSTRAINT swap_request_ibfk_5 FOREIGN KEY (schedule_id) REFERENCES public.duty_schedule(id) ON DELETE CASCADE;


--
-- TOC entry 3814 (class 2606 OID 34246)
-- Name: sys_dict_data sys_dict_data_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_dict_data
    ADD CONSTRAINT sys_dict_data_ibfk_1 FOREIGN KEY (dict_type_id) REFERENCES public.sys_dict_type(id) ON DELETE CASCADE;


--
-- TOC entry 3813 (class 2606 OID 34216)
-- Name: sys_employee sys_employee_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_employee
    ADD CONSTRAINT sys_employee_ibfk_1 FOREIGN KEY (dept_id) REFERENCES public.sys_dept(id) ON DELETE CASCADE;


--
-- TOC entry 3834 (class 2606 OID 34559)
-- Name: sys_role_menu sys_role_menu_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_role_menu
    ADD CONSTRAINT sys_role_menu_ibfk_1 FOREIGN KEY (role_id) REFERENCES public.sys_role(id) ON DELETE CASCADE;


--
-- TOC entry 3835 (class 2606 OID 34564)
-- Name: sys_role_menu sys_role_menu_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_role_menu
    ADD CONSTRAINT sys_role_menu_ibfk_2 FOREIGN KEY (menu_id) REFERENCES public.sys_menu(id) ON DELETE CASCADE;


--
-- TOC entry 3836 (class 2606 OID 34583)
-- Name: sys_user_role sys_user_role_ibfk_1; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT sys_user_role_ibfk_1 FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE;


--
-- TOC entry 3837 (class 2606 OID 34588)
-- Name: sys_user_role sys_user_role_ibfk_2; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.sys_user_role
    ADD CONSTRAINT sys_user_role_ibfk_2 FOREIGN KEY (role_id) REFERENCES public.sys_role(id) ON DELETE CASCADE;


--
-- 任务表结构调整
--
-- 添加attachments字段
ALTER TABLE pm_task ADD COLUMN attachments text;

-- 增加description字段长度以支持富文本内容
ALTER TABLE pm_task ALTER COLUMN description TYPE text;

-- 项目表结构调整
-- 删除旧的单个 deputy_owner_id 字段
ALTER TABLE pm_project DROP COLUMN IF EXISTS deputy_owner_id;

-- 创建代理负责人关联表
CREATE TABLE IF NOT EXISTS public.pm_project_deputy_owner (
    id SERIAL PRIMARY KEY,
    project_id BIGINT NOT NULL,
    employee_id BIGINT NOT NULL,
    create_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (project_id) REFERENCES public.pm_project(id) ON DELETE CASCADE,
    FOREIGN KEY (employee_id) REFERENCES public.sys_employee(id) ON DELETE CASCADE,
    UNIQUE(project_id, employee_id)
);

-- 创建索引
CREATE INDEX IF NOT EXISTS idx_pm_project_deputy_owner_project_id ON public.pm_project_deputy_owner(project_id);
CREATE INDEX IF NOT EXISTS idx_pm_project_deputy_owner_employee_id ON public.pm_project_deputy_owner(employee_id);

--
-- 自定义表格功能表
--

-- 自定义表格配置表
CREATE SEQUENCE public.pm_custom_table_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.pm_custom_table (
    id bigint NOT NULL,
    table_name character varying(100) NOT NULL,
    table_code character varying(50) NOT NULL,
    project_id bigint,
    description character varying(500),
    status smallint DEFAULT 1,
    create_by bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE public.pm_custom_table OWNER TO postgres;
ALTER TABLE ONLY public.pm_custom_table ALTER COLUMN id SET DEFAULT nextval('public.pm_custom_table_id_seq'::regclass);
ALTER TABLE ONLY public.pm_custom_table ADD CONSTRAINT pm_custom_table_pkey PRIMARY KEY (id);
CREATE INDEX idx_pm_custom_table_project_id ON public.pm_custom_table(project_id);
CREATE INDEX idx_pm_custom_table_code ON public.pm_custom_table(table_code);

COMMENT ON TABLE public.pm_custom_table IS '自定义表格配置表';
COMMENT ON COLUMN public.pm_custom_table.id IS '表格ID';
COMMENT ON COLUMN public.pm_custom_table.table_name IS '表格名称';
COMMENT ON COLUMN public.pm_custom_table.table_code IS '表格编码';
COMMENT ON COLUMN public.pm_custom_table.project_id IS '关联项目ID';
COMMENT ON COLUMN public.pm_custom_table.description IS '表格描述';
COMMENT ON COLUMN public.pm_custom_table.status IS '状态：0禁用，1启用';
COMMENT ON COLUMN public.pm_custom_table.create_by IS '创建人ID';
COMMENT ON COLUMN public.pm_custom_table.create_time IS '创建时间';
COMMENT ON COLUMN public.pm_custom_table.update_time IS '更新时间';

-- 自定义表格列表
CREATE SEQUENCE public.pm_custom_table_column_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.pm_custom_table_column (
    id bigint NOT NULL,
    table_id bigint NOT NULL,
    column_name character varying(100) NOT NULL,
    column_code character varying(50) NOT NULL,
    column_type character varying(20) DEFAULT 'text',
    column_width integer DEFAULT 150,
    required smallint DEFAULT 0,
    sort_order integer DEFAULT 0,
    options json,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE public.pm_custom_table_column OWNER TO postgres;
ALTER TABLE ONLY public.pm_custom_table_column ALTER COLUMN id SET DEFAULT nextval('public.pm_custom_table_column_id_seq'::regclass);
ALTER TABLE ONLY public.pm_custom_table_column ADD CONSTRAINT pm_custom_table_column_pkey PRIMARY KEY (id);
CREATE INDEX idx_pm_custom_table_column_table_id ON public.pm_custom_table_column(table_id);

COMMENT ON TABLE public.pm_custom_table_column IS '自定义表格列表';
COMMENT ON COLUMN public.pm_custom_table_column.id IS '列ID';
COMMENT ON COLUMN public.pm_custom_table_column.table_id IS '关联表格ID';
COMMENT ON COLUMN public.pm_custom_table_column.column_name IS '列名称';
COMMENT ON COLUMN public.pm_custom_table_column.column_code IS '列编码';
COMMENT ON COLUMN public.pm_custom_table_column.column_type IS '列类型：text文本，number数字，date日期，select下拉，person人员';
COMMENT ON COLUMN public.pm_custom_table_column.column_width IS '列宽度';
COMMENT ON COLUMN public.pm_custom_table_column.required IS '是否必填：0否，1是';
COMMENT ON COLUMN public.pm_custom_table_column.sort_order IS '排序';
COMMENT ON COLUMN public.pm_custom_table_column.options IS '下拉选项（JSON格式）';
COMMENT ON COLUMN public.pm_custom_table_column.create_time IS '创建时间';

-- 自定义表格数据行
CREATE SEQUENCE public.pm_custom_table_row_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.pm_custom_table_row (
    id bigint NOT NULL,
    table_id bigint NOT NULL,
    row_data json,
    sort_order integer DEFAULT 0,
    create_by bigint,
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
    update_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE public.pm_custom_table_row OWNER TO postgres;
ALTER TABLE ONLY public.pm_custom_table_row ALTER COLUMN id SET DEFAULT nextval('public.pm_custom_table_row_id_seq'::regclass);
ALTER TABLE ONLY public.pm_custom_table_row ADD CONSTRAINT pm_custom_table_row_pkey PRIMARY KEY (id);
CREATE INDEX idx_pm_custom_table_row_table_id ON public.pm_custom_table_row(table_id);

COMMENT ON TABLE public.pm_custom_table_row IS '自定义表格数据行';
COMMENT ON COLUMN public.pm_custom_table_row.id IS '行ID';
COMMENT ON COLUMN public.pm_custom_table_row.table_id IS '关联表格ID';
COMMENT ON COLUMN public.pm_custom_table_row.row_data IS '行数据（JSON格式）';
COMMENT ON COLUMN public.pm_custom_table_row.sort_order IS '排序字段';
COMMENT ON COLUMN public.pm_custom_table_row.create_by IS '创建人ID';
COMMENT ON COLUMN public.pm_custom_table_row.create_time IS '创建时间';
COMMENT ON COLUMN public.pm_custom_table_row.update_time IS '更新时间';

-- 任务与自定义行关联表
CREATE SEQUENCE public.pm_task_custom_row_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;

CREATE TABLE public.pm_task_custom_row (
    id bigint NOT NULL,
    task_id bigint NOT NULL,
    table_id bigint NOT NULL,
    row_id bigint NOT NULL,
    order_no character varying(255),
    title character varying(500),
    create_time timestamp without time zone DEFAULT CURRENT_TIMESTAMP
);

ALTER TABLE public.pm_task_custom_row OWNER TO postgres;
ALTER TABLE ONLY public.pm_task_custom_row ALTER COLUMN id SET DEFAULT nextval('public.pm_task_custom_row_id_seq'::regclass);
ALTER TABLE ONLY public.pm_task_custom_row ADD CONSTRAINT pm_task_custom_row_pkey PRIMARY KEY (id);
CREATE INDEX idx_pm_task_custom_row_task_id ON public.pm_task_custom_row(task_id);
CREATE INDEX idx_pm_task_custom_row_table_id ON public.pm_task_custom_row(table_id);
CREATE INDEX idx_pm_task_custom_row_row_id ON public.pm_task_custom_row(row_id);

COMMENT ON TABLE public.pm_task_custom_row IS '任务与自定义行关联表';
COMMENT ON COLUMN public.pm_task_custom_row.id IS '关联ID';
COMMENT ON COLUMN public.pm_task_custom_row.task_id IS '任务ID';
COMMENT ON COLUMN public.pm_task_custom_row.table_id IS '自定义表格ID';
COMMENT ON COLUMN public.pm_task_custom_row.row_id IS '自定义表格行ID';
COMMENT ON COLUMN public.pm_task_custom_row.order_no IS '单号';
COMMENT ON COLUMN public.pm_task_custom_row.title IS '标题';
COMMENT ON COLUMN public.pm_task_custom_row.create_time IS '创建时间';

-- Completed on 2026-02-24 20:59:48

--
-- PostgreSQL database dump complete
--

\unrestrict Ktio33LCUIbNfkVFWYN0IA5hWYcczvHJovEbN5VPeGIMhr6buT7budcp8qMztfg

