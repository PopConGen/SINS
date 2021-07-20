# coding: utf-8
import matplotlib
matplotlib.use('svg')
import matplotlib.pyplot as plt
import matplotlib.animation as animation
from matplotlib import gridspec
import numpy as np
import os
import argparse


class DemographyFrame:
    def __init__(self, generation, demomap):
        self.generation = generation
        self.demoMap = demomap


class LayerMaps:
    def __init__(self, _layer_name, _demography_frame_list):
        self.layer_name = _layer_name
        self.demography_frame_list = _demography_frame_list


class SimulationReplicateMap:
    def __init__(self, _simulation_idx, _layer_list):
        self.sim_idx = _simulation_idx
        self.layers = _layer_list


def get_params_from_file(base_simulation_path, sim_num_list):

    layers = []

    sim_name = base_simulation_path.strip().split('/')[-1]  # name of simulation is the last token from the path

    for simIdx in sim_num_list:
        with open(base_simulation_path+"/simulation_"+str(simIdx)+"/demography.txt", 'r') as demography_file:
            for line in demography_file:
                if len(line) > 1:
                    line_tokens = line.split(':')
                    if line_tokens[0] == "Layer name":
                        if not layers.__contains__(line_tokens[1].strip()):
                            layers.append(line_tokens[1].strip())

    opts_params = [base_simulation_path, sim_num_list, layers, sim_name]
    return opts_params


def simple_sstats_plot(simparams, myfig, gs):
    pathtostats =\
        simparams[0] +\
        "/simulation_" +\
        str(1) +\
        "/SummaryStatistics.txt"

    stat_table = np.genfromtxt(
        pathtostats,
        dtype="i8,i8,S5,f16,f16,f16,f16",
        names=True,
        missing_values='NA'
    )

    # color_sequence = ['#1f77b4', '#aec7e8', '#ff7f0e', '#ffbb78', '#2ca02c',
    #                   '#98df8a', '#d62728', '#ff9896', '#9467bd', '#c5b0d5',
    #                   '#8c564b', '#c49c94', '#e377c2', '#f7b6d2', '#7f7f7f',
    #                   '#c7c7c7', '#bcbd22', '#dbdb8d', '#17becf', '#9edae5']

    color_sequence = ["red", "blue", "grey", "orange"]

    genlist = list(set(stat_table["Generation"]))
    genlist.sort()

    subfig1 = myfig.add_subplot(gs[0, -2:])
    subfig1.set_yscale("linear")
    subfig1.set_ylabel("Heterozygosity", color="black")

    subfig2 = myfig.add_subplot(gs[1, -2:])
    subfig2.set_yscale("linear")
    subfig2.set_ylabel("MeanAlleleFrequency", color="black")

    subfig3 = myfig.add_subplot(gs[2, -2:])
    subfig3.set_yscale("log")
    subfig3.set_ylabel("NumAlleles", color="black")

    dictgen = {}
    for marker in set(stat_table["Marker"]):
        if marker == b'MT':
            dictgen[marker] = 0
        elif marker == b'Y':
            dictgen[marker] = 1
        elif marker == b'X':
            dictgen[marker] = 2
        else:
            dictgen[marker] = 3

    for marker, marker_value in dictgen.items():
        if marker != b'X':
            stat_table_marker = stat_table[stat_table["Marker"] == marker]
            subfig1.plot(stat_table_marker["Generation"], stat_table_marker["Heterozygosity"], linewidth=1.5,
                         color=color_sequence[marker_value], animated=False, zorder=1, alpha=0.5)
            subfig2.plot(stat_table_marker["Generation"], stat_table_marker["MeanAlleleFrequency"], linewidth=1.5,
                         color=color_sequence[marker_value], animated=False, zorder=1, alpha=0.5)
            subfig3.plot(stat_table_marker["Generation"], stat_table_marker["NumAlleles"], linewidth=1.5,
                         color=color_sequence[marker_value], animated=False, zorder=1, alpha=0.5)

    return [subfig1, subfig2, subfig3]


def parse_demography_maps(sim_params):
    base_path = sim_params[0]
    sim_list = sim_params[1]
    layer_list = sim_params[2]

    sim_replicates = []

    in_block = False

    for simIdx in sim_list:
        layer_obj_list = []
        for layerId in layer_list:
            layer_obj_list.append(LayerMaps(layerId, []))
        with open(base_path+"/simulation_"+str(simIdx)+"/demography.txt", 'r') as demography_file:
            generation = 0
            layer = ""
            demo_map = []

            for line in demography_file:
                if len(line) > 1:
                    if in_block:
                        if line.strip() == "[/map]":
                            in_block = False
                            # print(map)
                            map_array = np.array(demo_map)
                            map_array = map_array.astype(np.float)
                            for layer_obj in layer_obj_list:
                                if layer_obj.layer_name == layer:
                                    layer_obj.demography_frame_list.append(DemographyFrame(generation, map_array))

                        else:
                            demo_map.append(line.strip().split())
                    else:
                        line_tokens = line.split(':')
                        if line_tokens[0] == "Generation":
                            generation = int(line_tokens[1])
                        if line_tokens[0] == "Layer name":
                            layer = line_tokens[1].strip()
                        if line_tokens[0].strip() == "[map]":
                            in_block = True
                            demo_map = []

        sim_replicates.append(SimulationReplicateMap(simIdx, layer_obj_list))
    return sim_replicates


def get_max_value(map_constructed_object):
    # maxperlayer = [] perhaps implement max per layer?
    max_num = -1

    for replicate in map_constructed_object:
        for layer in replicate.layers:
            for frame in layer.demography_frame_list:
                if frame.demoMap.max() > max_num:
                    max_num = frame.demoMap.max()
    return max_num


def plot_maps(
        map_constructed_object,
        sim_params,
        verbose=False,
        plot_sum_stats=True,
        is_gif=False,
        saveimgingen=[],
        anim_fps=30,
        anim_dpi=100
):

    for replicate in map_constructed_object:
        for layer in replicate.layers:
            my_fig = plt.figure(1)
            gs = gridspec.GridSpec(3, 6)
            if plot_sum_stats:
                in_fig = my_fig.add_subplot(gs[:, :-2])
            else:
                in_fig = my_fig.add_subplot(gs[:, :])
            # in_fig = plt.subplot(111)
            myplt = plt.imshow(
                layer.demography_frame_list[0].demoMap,
                vmin=0,
                vmax=get_max_value(map_constructed_object),
                cmap=matplotlib.cm.afmhot,
            )
            in_fig.text(1, 0,
                        str(layer.demography_frame_list[0].generation).zfill(7),
                        verticalalignment='bottom',
                        horizontalalignment='right',
                        transform=in_fig.transAxes,
                        color='black',
                        backgroundcolor='white',
                        fontsize=8)
            plt.colorbar(orientation='vertical',)
            in_fig.xaxis.set_visible(False)
            in_fig.yaxis.set_visible(False)

            if plot_sum_stats:
                mysubplots = simple_sstats_plot(simparams=sim_params, myfig=my_fig, gs=gs)
                originalsize = len(mysubplots[0].lines)

            def update_fig(j):
                # print(j)
                # print(str(layer.demography_frame_list[j].generation))

                thisgen = layer.demography_frame_list[j].generation
                a = ""
                if plot_sum_stats:
                    for aplot in mysubplots:
                        if len(aplot.lines) > originalsize:
                            # doing this so that the "timeline" keeps moving
                            aplot.lines.remove(aplot.lines[-1])
                        # a=aplot.axvline(x=thisgen,animated=True,color=(0.7,0.7,0.7,0.5),zorder=0)
                        a = aplot.axvline(x=thisgen, animated=True, color=(0.1, 0.1, 0.1, 0.5), zorder=0)
                        # a.set_color((0.9,0.9,0.9,0.3))

                # myplt.set_array(layer.demography_frame_list[j].demoMap)
                myplt.set_data(layer.demography_frame_list[j].demoMap)
                b = in_fig.text(1, 0,
                                str(thisgen).zfill(7),
                                verticalalignment='bottom',
                                horizontalalignment='right',
                                transform=in_fig.transAxes,
                                color='black',
                                backgroundcolor='white',
                                fontsize=8)

                if saveimgingen.__contains__(thisgen):
                    png_name = str(thisgen).zfill(7) + "_" + sim_params[3] + "_" +\
                               layer.layer_name + "_simRep" + str(replicate.sim_idx) + ".png"
                    plt.savefig(os.path.join(sim_params[0], 'Plots', png_name), bbox_inches='tight')

                if verbose:
                    print("Plotting simulation "+str(replicate.sim_idx)+" Generation ", str(thisgen))
                if plot_sum_stats:
                    return [a, myplt, b]
                else:
                    return [myplt, b]

            my_fig.tight_layout()
            ani = animation.FuncAnimation(my_fig,
                                          update_fig,
                                          frames=len(layer.demography_frame_list),
                                          blit=True,
                                          repeat_delay=100)

            if not os.path.exists(os.path.join(sim_params[0], 'Plots')):
                os.mkdir(os.path.join(sim_params[0], 'Plots'))

            if is_gif:
                writer = animation.writers['imagemagick'](fps=anim_fps)
                plot_name = sim_params[3] + "_" + layer.layer_name + "_simRep" + str(replicate.sim_idx) + ".gif"
            else:
                writer = animation.writers['ffmpeg'](fps=anim_fps, extra_args=['-vcodec', 'libx264'])
                plot_name = sim_params[3] + "_" + layer.layer_name + "_simRep" + str(replicate.sim_idx) + ".mp4"

            # plt.show()
            ani.save(os.path.join(sim_params[0], 'Plots', plot_name), writer=writer, dpi=anim_dpi)

            # plt.savefig('./Plots/'+plot_name, bbox_inches='tight')
            plt.clf()


def main():
    parser = argparse.ArgumentParser(description="Make SINS demographic images/videos")
    group_format = parser.add_mutually_exclusive_group()
    group_verbose = parser.add_mutually_exclusive_group()
    group_format.add_argument("-gif", "--isGif", action="store_true", help="Output in gif format")
    group_format.add_argument("-mp4", "--isMp4", action="store_true", help="Output in mp4 format")
    group_verbose.add_argument("-v", "--verbose", action="store_true", help="Output information to screen")
    group_verbose.add_argument("-q", "--quiet", action="store_true", help="Output no information to screen")
    parser.add_argument("-path", "--projectPath", type=str, help="Path to your SINS project output folder")
    parser.add_argument("-sstats", "--plotSumStats", action="store_true",
                        help="Also output a plot with the summary statistics")
    parser.add_argument("-saveAt", "--savePngAtGeneration", action='append', type=int, default=[],
                        help="Define when you want to save a png (a static image) of your plot)")
    parser.add_argument("-simlist", "--simulationList", action='append', type=int, default=[],
                        help="Define which simulations should be analyzed")
    parser.add_argument("-fps", "--framesPerSecond", type=int, default=30,
                        help="Define how many frames per second the animation will have. Default = 30.")
    parser.add_argument("-dpi", "--dotsPerInch", type=int, default=100,
                        help="Define how many dots per inch the animation will have. Default = 100.")
    args = parser.parse_args()

    # map_params = get_params_from_file("/home/tmaie/ServerFolders/Elephant_ServerFolder/SINS_Output/25x25_25kGen_test", range(1, 2, 1))
    # map_params = get_params_from_file("/home/tmaie/NetBeansProjects/sins2/results/STANDARD_5x5_2Layer_test", range(1, 2, 1))
    # map_params = get_params_from_file("/home/tmaie/NetBeansProjects/sins2/results/25x25_25kGen", range(1,2,1))
    # map_params = get_params_from_file("/home/tmaie/ServerFolders/AFS_ServerFolder/SINS_Results/SINS_Output/r05_m005_k100_10x10_t6000_f3000", range(1,3,1))

    map_params = get_params_from_file(args.projectPath, args.simulationList)

    # Plotting demography map and sumstats
    parsed_map_data = parse_demography_maps(map_params)
    plot_maps(
        parsed_map_data,
        map_params,
        plot_sum_stats=args.plotSumStats,
        is_gif=args.isGif,
        saveimgingen=args.savePngAtGeneration,
        verbose=args.verbose,
        anim_fps=args.framesPerSecond,
        anim_dpi=args.dotsPerInch
    )

if __name__ == "__main__":
    main()
