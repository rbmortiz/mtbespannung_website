export interface StringTypes {
	string_id: number;
	name: string;
	sport: string;
	color: string;
	description: string;
	type: string;
	price: number;
	is_active: Boolean;
	created_at: string;
	updated_at: string;
}

export interface StringingOrder {
	order_id: number;
	user_id: number;
	customer_first_name: string | null;
	customer_last_name: string | null;
	customer_email: string | null;
	racket_name: string;
	additional_info: string | null;
	horizontal_kg: number | null;
	vertical_kg: number | null;
	string_id: number;
	status: string;
	price: number | null;
	created_at: string;
	updated_at: string;
}

export interface users {
	user_id: number;
	first_name: string;
	last_name: string;
	email: string;
	role: string;
	created_at: string;
	updated_at: string;
}
