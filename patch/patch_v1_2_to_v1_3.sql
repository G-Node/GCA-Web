/*
SQL script to copy the content of 'description', 'logo' URL and 'thumbnail' URL
from the 'conference' table to the new 'conftext' table. The columns above will be deprecated.
This script is required when migrating from GCA-Web v1.2 to v1.3.
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
