%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%
% PVS_full_length.m
% Written by Ravi Kedarasetti, 
% 
% Calculate patricle trajectories and mean flow speed 
% in a 3D model of the PVS 
% Generates Fig 3c
% To be used for the output of PVS_3D_full_length.class
%
% Written by Ravi Kedarasetti (2020), department of Engineering Science and
% Mechanics, Pennsylvania State University 
%% Problem parameters
Ro = 30E-6; %m
Lo = Ro;
c = 1;%m/s
f = 60/7; %Hz
tau = 1/f; % s
lam = c/f;
La = lam;%m
g3fac = 10;
g1 = 1;
g2 = 1;
g3 = La/Lo/g3fac;
uo =Ro/100;% percent radius
pwd = 40E-6;
R1 = Ro+pwd;
disp_scale_r =  uo/Lo/g1;
disp_scale_z =  uo/Lo/g3;
%%
tstep = tau/100; %s
reps = 5;
tstop = reps*tau;
nd = 3; % formulation dimensions
%% read the data
FileName = 'pvsFullLengthResults.txt';
the_data = dlmread(FileName);
nr = 21; % no of points in r
nz = 101; % no of points in z
%% Create a r-z grid
rgrid = the_data(1:nr,1);
zgrid = the_data(1:nr:end,nd-2+2);
[Rgrid, Zgrid] = meshgrid(rgrid, zgrid);
n_frames = tau/tstep;
%% Read displacement data
umr_data = reshape(the_data(:,4:6:end),nr,nz,n_frames);
umr_data = repmat(umr_data,1,1,reps);
umz_data = reshape(the_data(:,6:6:end),nr,nz,n_frames);
umz_data = repmat(umz_data,1,1,reps);
%% Read particle velocities
xcdotr_data = reshape(the_data(:, 7:6:end),nr,nz,n_frames);
xcdotr_data = repmat(xcdotr_data,1,1,reps);
xcdotz_data = reshape(the_data(:, 9:6:end),nr,nz,n_frames);
xcdotz_data = repmat(xcdotz_data,1,1,reps);
n_frames = n_frames*reps;
%% Particle trajectory for single particle
dist_from_end = 1e-3;%m
z_coord = zgrid(end) - dist_from_end/Lo/g3;
r_wall = 1+0.03;
r_mid = (1+R1/Ro)/2;
xwall = forward_euler_2d([r_wall, z_coord], Rgrid,Zgrid, umr_data, umz_data,xcdotr_data, xcdotz_data,tstep,uo,disp_scale_r, disp_scale_z);
xmid = forward_euler_2d([r_mid, z_coord], Rgrid,Zgrid, umr_data, umz_data,xcdotr_data, xcdotz_data,tstep,uo,disp_scale_r, disp_scale_z);

t = (1:n_frames)*tstep;
wall_disp_perc = 100*(xwall(:,1)-1);
part_pos_um = 1e6*Lo*g3*(xmid(:,2) - xmid(1,2));
net_disp = part_pos_um(end);

fig = figure();
set(fig, 'Position', [1 1 1200 800])
plot(t,part_pos_um,'b', 'LineWidth',2)
ylabel('Particle displacement (\mum)')
ax = gca;
scl = diff(ax.YLim)/diff(ax.XLim);
hold on
plot([t(1) t(end)], [part_pos_um(1) part_pos_um(end)],'b')
mean_vel = net_disp/(t(end) - t(1));
h = text(mean([t(1) t(end)]), mean([part_pos_um(1) part_pos_um(end)])- 0.025*diff(ax.YLim), ['Mean speed ' num2str(round(mean_vel,1)) '\mum/s'],...
    'FontSize' ,20,'HorizontalAlignment', 'center');
m1 = pbaspect;
m = atand(m1(2)*mean_vel/scl);
set(h, 'Rotation',m)
set(gca,'YColor','b')
set(gca, 'FontSize',14)
xlabel('time(s)')
saveas(fig, 'PVS_full_length_trajectory.png')
saveas(fig, 'PVS_full_length_trajectory.pdf')
%% Particle velocity for single particle
wall_vel = 1e6*Lo*g1*diff(xwall(:,1))/tstep;
part_vel = 1e6*Lo*g3*diff(xmid(:,2))/tstep;
lim1 = max(wall_vel(1:2*tau/tstep));
offset = 100;
fig = figure();
set(fig, 'Position', [1 1 1200 800])
set(fig, 'Color', 'w')
plot((t(1:2*tau/tstep) - t(offset))*f,wall_vel(1:2*tau/tstep), 'k', 'LineWidth',2)
ylabel('Wall velocity (\mum/s)')
ylim([-15 30])
yyaxis right
plot((t(1:2*tau/tstep) - t(offset))*f,part_vel(1:2*tau/tstep),'b', 'LineWidth',2)
ylim([-max(part_vel(1:2*tau/tstep)) 1.2*max(part_vel(1:2*tau/tstep))])
ylabel('Particle velocity (\mum/s)')
xlabel('fractional cycle')
xlim([0 1])
ylim([-15000 30000])
set(gca,'YColor','b')
set(gca, 'FontSize',24)
saveas(fig, 'Fig3c.png')
saveas(fig, 'Fig3c.pdf')