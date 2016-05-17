%% Compensation on (when excited) and off NOT USED CURRENTLY
clear all
load('compensationOff')
baseAngOff = baseAng;
pendAngOff = pendAng;
tOff = t;

load('compensationOn')
baseAngOn = baseAng;
pendAngOn = pendAng;
tOn = t;

% tOn largest
sizediff = length(tOn) - length(tOff)
plot(double(tOff - tOff(1))/1000, [baseAngOn(sizediff+1:end); baseAngOff])


%% Compensation off then turned on
clear all
close all
load('compensationOffOn')
plot(double(t - t(1))/1000, [baseAng; pendAng])
legend('\phi','\Theta')
axis([0 20 -0.4 0.4])
xlabel('time (s)')
ylabel('radians (rad)')
set(gca,'fontsize',30)

%% Convergence with and without excitation (manual)
clear all
close all
load('rlsconvergence')
times = double(t - t(1))/1000
index = find(times < 49 | times > 102)
plot([fc(index); fv(index); zeros(1,length(index))]')
axis([0 10000 -0.7 0.4])
xlabel('time units')
ylabel('parameters')
legend('f_c','f_v')

%% Step response (full (good) compensation)
clear all
close all
load('stepresponse')
times = double(t - t(1))/1000
%plot(times, [baseAng; baseAngVel; pendAng; pendAngVel])
%subplot(2,2,1)
plot(times, baseAng)
ylabel('Base angle, $\varphi$, rad','interpreter','latex');
xlabel('time (s)')
figure;
plot(times, baseAngVel)
ylabel('Base angle velocity, $\dot{\varphi}$, rad/s','interpreter','latex');
%subplot(2,2,3)
xlabel('time (s)')
figure;
plot(times, pendAng)
ylabel('Pendulum angle, \theta, rad');
%subplot(2,2,4)
xlabel('time (s)')
figure;
plot(times, pendAngVel)
ylabel('Pendulum angle velocity, $\dot{\theta}$, rad/s,','interpreter','latex');
%axis([0 10000 -0.7 0.4])
xlabel('time (s)')
%legend('f_c','f_v')


%% Step response viscous vs coloumb (full (good) compensation)
clear all
close all
load('stepresponse')
times = double(t - t(1))/1000
plot(times, baseAng)
axis([0 37 -0.5 4])
ylabel('Base angle, $\varphi$, rad','interpreter','latex');
xlabel('time (s)')

clear all
load('coloumbStepResponse')
times = double(t - t(1))/1000
figure()
plot(times, baseAng)
axis([0 37 -0.5 4])
ylabel('Base angle, $\varphi$, rad','interpreter','latex');
xlabel('time (s)')

%% VL, y - kalla den vad du vill

clear all
close all
load('VL')
times = double(t - t(1))/1000
subplot(2,1,1)
plot(times, VL);
subplot(2,1,2)
plot(times, uF);
%axis([0 10000 -0.7 0.4])
%xlabel('time units')
%ylabel('parameters')
%legend('f_c','f_v')

%% COLOUMB VL, y - kalla den vad du vill

clear all
%close all
load('VLcoloumb')
times = double(t - t(1))/1000
subplot(2,1,1)
plot(times, VL);
subplot(2,1,2)
plot(times, uF);
%axis([0 10000 -0.7 0.4])
%xlabel('time units')
%ylabel('parameters')
%legend('f_c','f_v')

%% Everything on
clear all
close all
load('everything2')
plot(double(t - t(1))/1000, [baseAng; pendAng])
legend('\phi','\theta')
axis([0 15 -0.4 0.4])
xlabel('time (s)')
ylabel('radians (rad)')
set(gca,'fontsize',30)

%% Everything on step
clear all
close all
load('stepResponse-best-nointegral')
plot(double(t - t(1))/1000, [baseAng; pendAng])
legend('\phi','\theta')
axis([0 14 -1 4])
xlabel('time (s)')
ylabel('radians (rad)')
set(gca,'fontsize',30)

%% Swingup
clear all
close all
load('swingup_without_frictioncompensator')
plot(double(t - t(1))/1000, [[baseAng(1:324)-baseAng(324) baseAng(325:end)]; [pendAng(1:293)-2*pi pendAng(295) pendAng(295:end)]])
legend('\phi','\theta')
axis([0 10 -6 1])
xlabel('time (s)')
ylabel('radians (rad)')
set(gca,'fontsize',30)