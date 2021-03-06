x = zeros(1,61);
y = zeros(1,61);
l = 0;
for i = 1000:1000:60*1000 
   l = l + 1;
   y(l) = sum(u(i+200:i+1000))/length(u(i+200:i+1000));
   x(l) = sum(phid(i+200:i+1000))/length(phid(i+200:i+1000));
end
figure 
plot(x,y,'o')
xlabel('$\dot{\varphi}$,rad/s','interpreter','latex')
ylabel('Friction')
%plot([x(1:2:28) x(35:2:61)],[y(1:2:28) y(35:2:61)],'o')
%figure 
%plot(phid(1000:end),u(1000:end),'.')
%plot([x(20:31) x(31:42)], [y(20:31) y(31:42)], '.')