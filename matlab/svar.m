%% 1
pendinit
eig(Afc)
%not stable in thetha thethaprim
ctrb(Afc,Bfc)
rank(ans)
%Controllable

eig(Afc-Bfc*[-7.5343 -1.3465 0 -0.2216])
% not stable in theta ????? ask
%% elips
ar=sqrt(0.62^2+9.4^2);
ar=ar*0.7;
br=sqrt(0.19^2+2.5^2);
br=br*0.7;
alfar=atan(9.4/0.62);