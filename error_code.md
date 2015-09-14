Error code is a bit coded integer.  The meaning of each bit starting from the least significant bit is as follows

  1. Bad Trial
  1. Eye Detection Error
  1. Unrecoverable Error
  1. Top Left Corner Error
  1. Top Right Corner Error
  1. Bottom Left Corner Error
  1. Bottom Right Corner Error

This should be interpreted as follows. An error code of 1 means the trial was coded as bad by the user. An error code of 3 means it was coded as bad AND it was an eye detection error. To recover all of the bits, use this function in matlab
```
%bitget(ErrorCode,1:8)

%For example,

bitget(3,1:8)

     1     1     0     0     0     0     0     0
```
Note that matlab returns the least significant bit on the left, not on the right.