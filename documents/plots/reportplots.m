%%
figure;
load('foconmuchexc')
plot(double(t(1:end-1) - t(1))/1000, [fc;fv;fo])
axis([0 35 -0.05 0.2])
legend('fc','fv','fo')
xlabel('t(s)')
title('Convergence of RLS parameters with much excitation')
%%
figure;
load('foconnoexc')
plot(double(t - t(1))/1000, [fc;fv;fo])
axis([0 35 -0.05 0.2])
legend('fc','fv','fo')
xlabel('t(s)')
title('Convergence of RLS parameters with little excitation')
%%
figure;
load('foconmediumexc')
plot(double(t - t(1))/1000, [fc;fv;fo])
axis([0 35 -0.05 0.2])
legend('fc','fv','fo')
xlabel('t(s)')
title('Convergence of RLS parameters with some excitation')
%%
figure
load('withfrictionoffsetposcost100_1000_1500_2000_2500')
plot(double(t - t(1))/1000, [baseAng; pendAng])
axis([0 70 -0.3 0.1])
legend('\phi','\theta')
xlabel('t(s)')
title('Limit cycles with friction offset. (changed cost from 100->1000)')