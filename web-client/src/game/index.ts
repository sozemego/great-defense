import { CITY_SELECTED, SECTION_DISPLAY_SELECTED } from "./actions";

export interface ResourceDatas {
  [key: string]: ResourceData;
}

export interface ResourceData {
  count: number;
  averagePrice: number;
  saturation: number;
  factories: number;
}

export enum Section {
  FACTORY = "FACTORY",
  TRUCK = "TRUCK",
  CITY = "CITY",
  GAME_EVENT = "GAME_EVENT"
}

export type SectionName = Record<Section, string>;

export type SectionSelection = Record<Section, boolean>;

export interface GameState {
  selectedSections: SectionSelection;
  selectedCity: string;
}

export interface SectionDisplaySelected {
  type: typeof SECTION_DISPLAY_SELECTED;
  selections: SectionSelection;
}

export interface CitySelectedAction {
  type: typeof CITY_SELECTED;
  cityId: string;
}

export type GameAction = SectionDisplaySelected | CitySelectedAction;

export interface GameProps {
  height: number;
}
