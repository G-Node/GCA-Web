/*
SQL script to copy description, logo URL and thumbnail URL to new conftext table.
The columns above will be deprecated.
*/

INSERT INTO conftext (uuid, cttype, "text", conference_uuid)
SELECT uuid_generate_v1(), 'description', description, uuid FROM conference
WHERE description IS NOT NULL AND NOT EXISTS (
    SELECT cttype, conference_uuid FROM conftext
    WHERE conftext.conference_uuid = conference.uuid
    AND conftext.cttype = 'description'
) ON CONFLICT DO NOTHING;

INSERT INTO conftext (uuid, cttype, "text", conference_uuid)
SELECT uuid_generate_v1(), 'logo', logo, uuid FROM conference
WHERE logo IS NOT NULL AND NOT EXISTS (
    SELECT cttype, conference_uuid FROM conftext
    WHERE conftext.conference_uuid = conference.uuid
    AND conftext.cttype = 'logo'
) ON CONFLICT DO NOTHING;

INSERT INTO conftext (uuid, cttype, "text", conference_uuid)
SELECT uuid_generate_v1(), 'thumbnail', thumbnail, uuid FROM conference
WHERE thumbnail IS NOT NULL AND NOT EXISTS (
    SELECT cttype, conference_uuid FROM conftext
    WHERE conftext.conference_uuid = conference.uuid
    AND conftext.cttype = 'thumbnail'
) ON CONFLICT DO NOTHING;