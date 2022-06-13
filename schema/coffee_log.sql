--
-- PostgreSQL database dump
--

-- Dumped from database version 14.3
-- Dumped by pg_dump version 14.3

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE postgres;
--
-- Name: postgres; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE postgres WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE = 'en_US.UTF-8';


ALTER DATABASE postgres OWNER TO postgres;

\connect postgres

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: DATABASE postgres; Type: COMMENT; Schema: -; Owner: postgres
--

COMMENT ON DATABASE postgres IS 'default administrative connection database';


SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: brew; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.brew (
    id integer NOT NULL,
    user_id integer NOT NULL,
    "timestamp" timestamp without time zone NOT NULL,
    coffee_id integer NOT NULL,
    water_id integer NOT NULL,
    brewer_id integer NOT NULL,
    filter_id integer NOT NULL,
    vessel_id integer NOT NULL,
    coffee_mass numeric(10,4) NOT NULL,
    water_mass numeric(10,4) NOT NULL
);


ALTER TABLE public.brew OWNER TO postgres;

--
-- Name: brew_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.brew_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.brew_id_seq OWNER TO postgres;

--
-- Name: brew_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.brew_id_seq OWNED BY public.brew.id;


--
-- Name: brewer; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.brewer (
    id integer NOT NULL,
    user_id integer NOT NULL,
    name character varying(45) NOT NULL
);


ALTER TABLE public.brewer OWNER TO postgres;

--
-- Name: brewer_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.brewer_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.brewer_id_seq OWNER TO postgres;

--
-- Name: brewer_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.brewer_id_seq OWNED BY public.brewer.id;


--
-- Name: coffee; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.coffee (
    id integer NOT NULL,
    user_id integer NOT NULL,
    name character varying(45) NOT NULL
);


ALTER TABLE public.coffee OWNER TO postgres;

--
-- Name: coffee_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.coffee_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.coffee_id_seq OWNER TO postgres;

--
-- Name: coffee_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.coffee_id_seq OWNED BY public.coffee.id;


--
-- Name: filter; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.filter (
    id integer NOT NULL,
    user_id integer NOT NULL,
    name character varying(45) NOT NULL
);


ALTER TABLE public.filter OWNER TO postgres;

--
-- Name: filter_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.filter_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.filter_id_seq OWNER TO postgres;

--
-- Name: filter_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.filter_id_seq OWNED BY public.filter.id;


--
-- Name: user; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public."user" (
    id integer NOT NULL,
    username character varying(15) NOT NULL,
    password_hash character varying(60) NOT NULL
);


ALTER TABLE public."user" OWNER TO postgres;

--
-- Name: user_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.user_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.user_id_seq OWNER TO postgres;

--
-- Name: user_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.user_id_seq OWNED BY public."user".id;


--
-- Name: vessel; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.vessel (
    id integer NOT NULL,
    user_id integer NOT NULL,
    name character varying(45) NOT NULL
);


ALTER TABLE public.vessel OWNER TO postgres;

--
-- Name: vessel_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.vessel_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.vessel_id_seq OWNER TO postgres;

--
-- Name: vessel_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.vessel_id_seq OWNED BY public.vessel.id;


--
-- Name: water; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.water (
    id integer NOT NULL,
    user_id integer NOT NULL,
    name character varying(45) NOT NULL
);


ALTER TABLE public.water OWNER TO postgres;

--
-- Name: water_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.water_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER TABLE public.water_id_seq OWNER TO postgres;

--
-- Name: water_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.water_id_seq OWNED BY public.water.id;


--
-- Name: brew id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.brew ALTER COLUMN id SET DEFAULT nextval('public.brew_id_seq'::regclass);


--
-- Name: brewer id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.brewer ALTER COLUMN id SET DEFAULT nextval('public.brewer_id_seq'::regclass);


--
-- Name: coffee id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coffee ALTER COLUMN id SET DEFAULT nextval('public.coffee_id_seq'::regclass);


--
-- Name: filter id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter ALTER COLUMN id SET DEFAULT nextval('public.filter_id_seq'::regclass);


--
-- Name: user id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user" ALTER COLUMN id SET DEFAULT nextval('public.user_id_seq'::regclass);


--
-- Name: vessel id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vessel ALTER COLUMN id SET DEFAULT nextval('public.vessel_id_seq'::regclass);


--
-- Name: water id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.water ALTER COLUMN id SET DEFAULT nextval('public.water_id_seq'::regclass);


--
-- Data for Name: brew; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.brew (id, user_id, "timestamp", coffee_id, water_id, brewer_id, filter_id, vessel_id, coffee_mass, water_mass) FROM stdin;
\.


--
-- Data for Name: brewer; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.brewer (id, user_id, name) FROM stdin;
\.


--
-- Data for Name: coffee; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.coffee (id, user_id, name) FROM stdin;
\.


--
-- Data for Name: filter; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.filter (id, user_id, name) FROM stdin;
\.


--
-- Data for Name: user; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public."user" (id, username, password_hash) FROM stdin;
\.


--
-- Data for Name: vessel; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.vessel (id, user_id, name) FROM stdin;
\.


--
-- Data for Name: water; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.water (id, user_id, name) FROM stdin;
\.


--
-- Name: brew_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.brew_id_seq', 1, false);


--
-- Name: brewer_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.brewer_id_seq', 1, false);


--
-- Name: coffee_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.coffee_id_seq', 1, false);


--
-- Name: filter_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.filter_id_seq', 1, false);


--
-- Name: user_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.user_id_seq', 1, false);


--
-- Name: vessel_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.vessel_id_seq', 1, false);


--
-- Name: water_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.water_id_seq', 1, false);


--
-- Name: brew brew_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.brew
    ADD CONSTRAINT brew_pkey PRIMARY KEY (id);


--
-- Name: brewer brewer_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.brewer
    ADD CONSTRAINT brewer_pkey PRIMARY KEY (id);


--
-- Name: coffee coffee_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coffee
    ADD CONSTRAINT coffee_pkey PRIMARY KEY (id);


--
-- Name: filter filter_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter
    ADD CONSTRAINT filter_pkey PRIMARY KEY (id);


--
-- Name: user user_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_pkey PRIMARY KEY (id);


--
-- Name: user user_username_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public."user"
    ADD CONSTRAINT user_username_key UNIQUE (username);


--
-- Name: vessel vessel_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vessel
    ADD CONSTRAINT vessel_pkey PRIMARY KEY (id);


--
-- Name: water water_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.water
    ADD CONSTRAINT water_pkey PRIMARY KEY (id);


--
-- Name: brew brew_brewer_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.brew
    ADD CONSTRAINT brew_brewer_id_fkey FOREIGN KEY (brewer_id) REFERENCES public.brewer(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: brew brew_coffee_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.brew
    ADD CONSTRAINT brew_coffee_id_fkey FOREIGN KEY (coffee_id) REFERENCES public.coffee(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: brew brew_filter_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.brew
    ADD CONSTRAINT brew_filter_id_fkey FOREIGN KEY (filter_id) REFERENCES public.filter(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: brew brew_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.brew
    ADD CONSTRAINT brew_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: brew brew_vessel_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.brew
    ADD CONSTRAINT brew_vessel_id_fkey FOREIGN KEY (vessel_id) REFERENCES public.vessel(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: brew brew_water_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.brew
    ADD CONSTRAINT brew_water_id_fkey FOREIGN KEY (water_id) REFERENCES public.water(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: brewer brewer_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.brewer
    ADD CONSTRAINT brewer_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: coffee coffee_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.coffee
    ADD CONSTRAINT coffee_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: filter filter_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.filter
    ADD CONSTRAINT filter_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: vessel vessel_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.vessel
    ADD CONSTRAINT vessel_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: water water_user_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.water
    ADD CONSTRAINT water_user_id_fkey FOREIGN KEY (user_id) REFERENCES public."user"(id) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

