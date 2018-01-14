[![Build Status](https://travis-ci.org/anob3it/person-uuid.svg?branch=master)](https://travis-ci.org/anob3it/person-uuid)

# UUID encoding of Swedish identity numbers.

An encoding of Swedish identity numbers for organisations, individuals,
etc. in a Version 1 date-time and MAC address UUID. The MAC address is fixed
and uniquely identifies this UUID as encoding a Swedish identity number.
In addition the least significant bit of `N` must be set to 1.

The identity number is stored in the `time_low` and `time_mid` fields
encoded with 4 bits per digit to be readable as the original identity
in the canonical UUID hex representation.

A serial number is stored in `time_hi` to support multiple businesses registered
to the same individual natural person.

```
                        ________________   _______________
                       /                \ /               \
                       00112233 4455 6677 8899 aabbccddeeff
                       -------- ---- ---- ---- ------------
                       xxxxxxxx-xxxx-Mxxx-Nxxx-xxxxxxxxxxxx
                       iiiiiiii-iiii-1nnn-9xxx-d49a20d06c1a
                       \___________/ |\_/ |  | \__________/
                             |       | |  |  |       |
                         id number   | |  |  | fixed MAC address
      stored as 1 digit per nybble   | |  |  |
                                     | |  |  |
version 1: date-time and MAC address / |  |  |
                                       |  |  |
                         serial number /  |  |
                                          |  |
   variant 1: 10x with x=0 -> 100 (hex 8) |  |
    lsb of N = 1, yielding N=1001 (hex 9) /  |
                                             |
                     x=reserved (must be 0)  /
```

## Examples

Enterprise identity number: `00556809-9963-1000-9000-d49a20d06c1a`

Social security number: `19410617-7753-1000-9000-d49a20d06c1a`



