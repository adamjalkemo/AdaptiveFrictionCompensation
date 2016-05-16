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
%%
load('everything-rlsConverge-excitation')
plot(double(t - t(1))/1000, [fc;fv;fo])
axis([0 20 -0.05 0.2])
legend('fc','fv','fo')
xlabel('t(s)')
title('Convergence of RLS parameters with excitation and integrating part')
print('rlsconvoffset','-depsc')
%%
load('everything-rlsConverge-without-excitation')
plot(double(t - t(1))/1000, [fc;fv;fo])
axis([0 20 -0.05 0.2])
legend('fc','fv','fo')
xlabel('t(s)')
title('Convergence of RLS parameters without excitation and integrating part')
%% Ta inte denna
load('everything')
plot(double(t - t(1))/1000, [baseAng; pendAng])
legend('\phi','\Theta')
axis([0 20 -0.4 0.4])
xlabel('time (s)')
ylabel('radians (rad)')
%% k?r denna
load('everything2')
plot(double(t - t(1))/1000, [baseAng; pendAng])
legend('\phi','\Theta')
axis([0 16 -0.4 0.4])
xlabel('time (s)')
ylabel('radians (rad)')
%%
load('everything_step')
times = double(t - t(1))/1000;
plot(times, baseAng)
axis([0 10 -0.5 4])
ylabel('Base angle, $\varphi$, rad','interpreter','latex');
xlabel('time (s)')
print('srphiall','-depsc')
%%
load('everything_step')
times = double(t - t(1))/1000;
plot(times, baseAngVel)
axis([0 10 -10 10])
ylabel('Base angle velocity, $\dot{varphi}$, rad/s','interpreter','latex');
xlabel('time (s)')
print('srphidotall','-depsc')
%%
load('everything_step')
times = double(t - t(1))/1000;
plot(times, baseAngVel)
axis([0 10 -10 10])
ylabel('Base angle velocity, $\dot{varphi}$, rad/s','interpreter','latex');
xlabel('time (s)')
print('srphidotall','-depsc')
% plot(times, baseAng)
% ylabel('Base angle, $\varphi$, rad','interpreter','latex');
% xlabel('time (s)')
% figure;
% plot(times, baseAngVel)
% ylabel('Base angle velocity, $\dot{\varphi}$, rad/s','interpreter','latex');
% subplot(2,2,3)
% xlabel('time (s)')
% figure;
% plot(times, pendAng)
% ylabel('Pendulum angle, \theta, rad');
% subplot(2,2,4)
% xlabel('time (s)')
% figure;
% plot(times, pendAngVel)
% ylabel('Pendulum angle velocity, $\dot{\theta}$, rad/s,','interpreter','latex');
% axis([0 10000 -0.7 0.4])
% xlabel('time (s)')
% legend('f_c','f_v')
%% VLUF offset
load('everything2')
times = double(t - t(1))/1000
subplot(2,1,1)
plot(times, VL);
subplot(2,1,2)
plot(times, uF);
%axis([0 10000 -0.7 0.4])
%xlabel('time units')
%ylabel('parameters')
%legend('f_c','f_v')

max(uF)
min(uF)