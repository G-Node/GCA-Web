/*
SQL script to copy description, logo URL and thumbnail URL to new conftext table.
The columns above will be deprecated.
*/
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
SELECT uuid_generate_v1();

CREATE TABLE IF NOT EXISTS conftext(
    "uuid" varchar(255) NOT NULL DEFAULT uuid_generate_v1(),
    ctType varchar(255) NOT NULL,
    "text" varchar(255),
    conference_uuid varchar(255) NOT NULL
);

INSERT INTO conftext (cttype, "text", conference_uuid)
SELECT 'description', description, uuid FROM conference
WHERE description IS NOT NULL AND NOT EXISTS (
    SELECT cttype, conference_uuid FROM conftext
    WHERE conftext.conference_uuid = conference.uuid
    AND conftext.cttype = 'description'
) ON CONFLICT DO NOTHING;

INSERT INTO conftext (cttype, text, conference_uuid)
SELECT 'logo', logo, uuid FROM conference
WHERE logo IS NOT NULL AND NOT EXISTS (
    SELECT cttype, conference_uuid FROM conftext
    WHERE conftext.conference_uuid = conference.uuid
    AND conftext.cttype = 'logo'
) ON CONFLICT DO NOTHING;

INSERT INTO conftext (cttype, text, conference_uuid)
SELECT 'thumbnail', thumbnail, uuid FROM conference
WHERE thumbnail IS NOT NULL AND NOT EXISTS (
    SELECT cttype, conference_uuid FROM conftext
    WHERE conftext.conference_uuid = conference.uuid
    AND conftext.cttype = 'thumbnail'
) ON CONFLICT DO NOTHING;

SELECT * FROM Conference;
SELECT * FROM conftext;

/*
ALTER TABLE conference
DROP COLUMN IF EXISTS description;
ALTER TABLE conference
DROP COLUMN IF EXISTS logoUrl;
ALTER TABLE conference
DROP COLUMN IF EXISTS thumbnailUrl;
*/