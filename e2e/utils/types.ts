export interface WellEntity {
	id: string
	name: string
	collection: string
	latitude: number | null
	longitude: number | null
}

export interface WellBoundariesEntity {
	startMs: number | null
	endMs: number | null
}

export interface Well extends WellEntity, WellBoundariesEntity {}

export interface TimeseriesPoint {
	timestamp: number
	temperature: number
	pressure: number
	oilRate: number
}
