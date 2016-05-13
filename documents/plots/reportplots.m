figure;
load('frictionoffsetconvergence')
plot(double(t - t(1))/1000, [fc;fv;fo])

figure
loadload('frictionoffsetlimitcycle')
plot(double(t - t(1))/1000, [baseAng; pendAng])